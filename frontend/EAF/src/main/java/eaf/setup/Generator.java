package eaf.setup;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.rects.OptionsFieldRect;
import eaf.rects.multi.ArrayRect;
import eaf.rects.multi.ClassRect;
import eaf.ui.panels.ConstantPane;
import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

import static eaf.models.ClassType.getUniqueImports;
import static eaf.rects.Rect.stringPadding;
import static eaf.ui.UiUtil.repeatString;

public class Generator extends Preset {



    public Generator() {
        requiredRectNames = new ArrayList<>();
        requiredRectNames.add("de.eaf.generator.generator");
    }


    @Override
    public void generateFiles(String folder, RectPanel panel) {
        LogManager.println(LogManager.preset() + LogManager.write() + LogManager.data() + " Writing EvoAl Data ...");
        FileManager.write(Main.dataPanel.toString("generator"), folder + "/generator.ddl");
        LogManager.println(LogManager.preset() + LogManager.write() + LogManager.ol()  + " Writing EvoAl Script ...");
        FileManager.write(rectPanelConversion(panel), folder+ "/generator.generator");
    }

    public String rectPanelConversion(RectPanel panel) {
        String res = "";
        var rects = panel.getRects();
        ClassRect base  = (ClassRect) rects.get(0);



        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();
        for (var r : rects) {
            panel.getAllRects(classesNeededForScript, r);
        }

        String constants = "";
        ArrayList<ClassType> filterImports = new ArrayList<>();
        for (var i : classesNeededForScript) {
            if (!i.pack.contains("de.eaf.generator") && !i.pack.contains("de.eaf.bridge") ) {
                filterImports.add(i);
            }
        }

        var imports = getUniqueImports(filterImports);
        for (var r : ConstantPane.getUsedConstants()) {
            if (!r.pack.isEmpty()) {
                if (!imports.contains(r.pack)) {
                    imports += "import \"definitions\" from " + r.pack + ";\n";
                }
            }
            else {
                constants += stringPadding + "const " + r.type + " " + r.name + " := " + r.value + ";\n";
            }

        }
        if (!constants.isEmpty()) {
            constants = "\n" + constants + "\n";
        }

        res += imports + "\n";
        res += "import \"data\" from 'generator';\n\n";
        res += "module 'generator' {\n";
        res += constants;

        String pipelines = "";
        for (var _pipeline : ((ArrayRect)base.getSubRectByName("pipelines")).getSubRects()) {
            var pipeline = (ClassRect) _pipeline;
            var name = pipeline.getSubRectByName("name").toString(1).replace("\"", "");
            if (pipelines.isEmpty()) {
                pipelines = "pipeline '" + name + "'";
            }
            else {
                pipelines += ", pipeline '" + name + "'";
            }
            res += repeatString(stringPadding, 1) + "pipeline '" + name + "' [\n";
            for (var _step : ((ArrayRect)pipeline.getSubRectByName("steps")).getSubRects()) {
                var step = (ClassRect) _step;
                res += repeatString(stringPadding, 2) + "step {\n";
                var component =  ((ClassRect)step.getSubRectByName("component"));
                res += repeatString(stringPadding, 3) + "component '" + SyntaxTree.toSimpleName(component.clazz.name) + "' {\n";
                for (int i = 0; i < component.getSubRects().length; i++) {
                    res += repeatString(stringPadding, 4) + "'" + component.names[i] + "' := " + component.getSubRects()[i].toString(0) + ";\n";
                }
                res += repeatString(stringPadding, 3) + "}\n";


                String buffer = "";
                buffer += repeatString(stringPadding, 3) + "reads [";
                boolean flag = false;
                for (var _read : ((ArrayRect)step.getSubRectByName("reads")).getSubRects()) {
                    flag = true;
                    var read = (OptionsFieldRect) _read;
                    if (buffer.equals( repeatString(stringPadding, 3) + "reads [")) {
                        buffer += read.toString(0);
                    }
                    else {
                        buffer += ", " + read.toString(0);
                    }
                }
                buffer += "];\n";
                if (flag) {
                    res += buffer;
                }


                String buffer2 = "";
                buffer2 += repeatString(stringPadding, 3) + "writes [";
                boolean flag2 = false;
                for (var _writes : ((ArrayRect)step.getSubRectByName("writes")).getSubRects()) {
                    flag2 = true;
                    var writes = (OptionsFieldRect) _writes;
                    if (buffer2.equals(repeatString(stringPadding, 3) + "writes [")) {
                        buffer2 += writes.toString(0);
                    }
                    else {
                        buffer2 += ", " + writes.toString(0);
                    }
                }
                buffer2 += "];\n";
                if (flag2) {
                    res += buffer2;
                }



                res += repeatString(stringPadding, 2) + "}\n";

            }

            res += repeatString(stringPadding, 1) + "]\n";
        }


        res += repeatString(stringPadding, 1) + "write \"" + base.getSubRectByName("output").toString(0).replace("\"", "") + "\" with " + base.getSubRectByName("samples").toString(0) + " samples from executing [" + pipelines + "];\n";


        res += "}";

        return res;
    }

    @Override
    public boolean implementationError() {
        return false;
    }

    @Override
    public String executionLine() {
        return "$SHELL $EVOAL_HOME/bin/evoal-generator.sh . generator.generator";
    }

    @Override
    public boolean postStart() {
        return true;
    }

    @Override
    public void postStop() {
    }
    @Override
    public String getName() {
        return "generator";
    }

    @Override
    public String getDisplayName() {
        return "Generator";
    }

    @Override
    public String shName() {
        return "generate";
    };
}
