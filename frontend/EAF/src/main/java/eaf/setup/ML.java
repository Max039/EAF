package eaf.setup;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.process.GenerationTracker;
import eaf.rects.Rect;
import eaf.rects.TextFieldRect;
import eaf.rects.multi.ArrayRect;
import eaf.rects.multi.ClassRect;
import eaf.ui.panels.ConstantPane;
import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

import static eaf.models.ClassType.getUniqueImports;

public class ML extends Preset {



    public ML() {
        requiredRectNames = new ArrayList<>();
        requiredRectNames.add("machine-learning");
    }


    @Override
    public void generateFiles(String folder, RectPanel panel) {
        System.out.println(LogManager.preset() + LogManager.write() + LogManager.data() + " Writing EvoAl Data ...");
        FileManager.write(Main.dataPanel.toString(), folder + "/config.ddl");
        System.out.println(LogManager.preset() + LogManager.write() + LogManager.ol()  + " Writing EvoAl Script ...");
        FileManager.write(rectPanelConversion(panel), folder+ "/config.mll");
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
            if (!i.pack.contains("de.eaf.mll")) {
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
                constants += Rect.stringPadding + "const " + r.type + " " + r.name + " := " + r.value + ";\n";
            }

        }
        if (!constants.isEmpty()) {
            constants = "\n" + constants + "\n";
        }

        res += imports + "\n";
        res += "import \"data\" from 'config';\n\n";
        res += "module 'config' {\n";
        res += constants;


        res += "prediction svr\n";
        for (var _map : ((ArrayRect)base.getSubRectByName("maps")).getSubRects()) {
            var map = (ClassRect) _map;
            String source = map.getSubRectByName("source").toString(0).replace("data ", "") + "\n";
            res += "maps " + source;

            String buffer = "";
            String to = "";
            buffer += "using\n";
            buffer += "layer transfer\n";
            buffer += "with\n";
            for (var _func : ((ArrayRect)((ClassRect)map.getSubRectByName("transfer")).getSubRectByName("mapping")).getSubRects()) {
                var func = (ClassRect) _func;
                buffer += "function '" + func.getSubRectByName("function").clazz.name + "'\n";
                buffer += "mapping " + source;
                String target = func.getSubRectByName("target").toString(0).replace("data ", "");
                buffer += "to '" + target + "'\n";
                buffer += "with parameters\n";
                if (to.isEmpty()) {
                    to = "'" + target + "'";
                }
                else {
                    to += ", '" + target + "'";
                }
                var v = ((ClassRect)func.getSubRectByName("function"));
                for (int i = 0; i < v.getSubRects().length; i++) {
                    buffer += "'" + v.names[i] + "' := " + v.getSubRects()[i].toString(0) + ";\n";
                }
            }
            res += "to "+ to + " \n";
            res += buffer;
        }


        res += "predict svr from " + base.getSubRectByName("data-input-path").toString(0) + "\n";
        res += "and measure\n";
        for (var _measure : ((ArrayRect)base.getSubRectByName("measures")).getSubRects()) {
            var measure = (ClassRect) _measure;
            res += "'" + measure.clazz.name + "'" + "(";
            int i = 0;
            for (var _param :  measure.getSubRects()) {
                if (i > 0) {
                    res += ", ";
                }
                var param = (TextFieldRect) _param;
                res += param.textBox.getText() + "\n";
                i++;
            }
            res += ");\n";
        }
        res += "end\n";
        res += "and store to " + base.getSubRectByName("data-output-path").toString(0) + "\n";






        res += "}";

        return res;
    }

    @Override
    public boolean implementationError() {
        return false;
    }

    @Override
    public String executionLine() {
        return "$SHELL $EVOAL_HOME/bin/evoal-training.sh . config.mll";
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
        return "ml";
    }
}
