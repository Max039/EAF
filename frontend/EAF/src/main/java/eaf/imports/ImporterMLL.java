package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.Module;
import eaf.rects.OptionsFieldRect;
import eaf.rects.RectFactory;
import eaf.rects.TextFieldRect;
import eaf.rects.multi.ArrayRect;
import eaf.rects.multi.ClassRect;
import eaf.setup.Preset;

import java.io.File;
import java.util.ArrayList;

import static eaf.Main.savesPath;

public class ImporterMLL extends  Importer {

    public void importFile(File file, String path) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];

        loadDDLFiles(file);

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        content = SyntaxTree.removeComments(content);

        var base = (ClassRect) RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.mll.machine-learning"));

        var maps = (ClassRect) ((ArrayRect) base.getSubRectByName("maps")).getSubRects()[0];


        var functions = (ArrayRect) ((ClassRect)maps.getSubRectByName("transfer")).getSubRectByName("mapping");
        functions.removeLast();

        var source = ((OptionsFieldRect) maps.getSubRectByName("source"));
        source.refreshComboBoxOptions();
        source.comboBox.setSelectedItem(SyntaxTree.getFormatParts(content, "maps", "to").get(0).trim().replace("\n", ""));
        source.refreshComboBoxOptions();
        int i = 1;
        for (var function : SyntaxTree.getFormatParts(content, "function", "predict", "for", "function")) {
            i++;
            var nameAndRest = function.split("mapping", 2);
            var mappingAndRest = nameAndRest[1].split("to", 2);
            var toAndRest = mappingAndRest[1].split("with", 2);

            var name = nameAndRest[0].trim().replace("\n", "");
            var mapping = mappingAndRest[0].trim().replace("\n", "");
            var to = toAndRest[0].trim().replace("\n", "");


            var parametersParts = toAndRest[1].split("parameters", 2)[1];
            var parameters = parametersParts.split(";");

            var resolve = tempModule.resolveClass(name);
            String clazz = "";

            for (var p : parameters) {
                p = p.trim().replace("\n", "");
                if (!p.isEmpty()) {
                    clazz += p + ";\n";
                }
            }

            var al = SyntaxTree.processContentOfType(resolve.instance(), clazz, tempModule);
            var rec = RectFactory.getRectFromClassType(al.instance);

            var fmap = (ClassRect) RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.mll.function-mapping"));
            fmap.setIndex(0, rec);

            var target = ((OptionsFieldRect) fmap.getSubRectByName("target"));
            target.refreshComboBoxOptions();
            target.comboBox.setSelectedItem(to);
            target.refreshComboBoxOptions();


            functions.addElement(fmap, functions.getSubRects().length);
        }


        var var = SyntaxTree.getFormatParts(content, "for", "loop");
        ClassRect config;

        if (!var.isEmpty()) {
            config = RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.mll.advanced-config"));
            var parts = var.get(0).split("in");
            var varname = parts[0];
            var in = parts[1].replace("[", "").replace("]", "").split("to");


            var cnt = ((TextFieldRect) config.getSubRectByName("$loop-counter"));
            cnt.setTextBox(varname.trim().replace("\n", ""));

            var start = ((TextFieldRect) config.getSubRectByName("loop-start"));
            start.setTextBox(in[0].trim().replace("\n", ""));

            var end = ((TextFieldRect) config.getSubRectByName("loop-end"));
            end.setTextBox(in[1].trim().replace("\n", ""));
        }
        else {
            config = RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.mll.simple-config"));
        }


        var measure = SyntaxTree.getFormatParts(content, "measure", "end");
        var arr = ((ArrayRect) config.getSubRectByName("measures"));
        arr.removeLast();
        for (var func : measure.get(0).split(";")) {
            func = func.trim();
            if (func.contains("(")) {
                var funcparts = func.split("\\(");
                ClassRect funcrec = RectFactory.getRectFromClassType(tempModule.resolveClass(funcparts[0].trim()));
                if (!funcparts[1].trim().isEmpty()) {
                    var args = funcparts[1].split(",");
                    for (int j = 0; j < args.length; j++) {
                        var v = args[j].replace(")", "").trim();
                        if (!v.isEmpty()) {
                            ((TextFieldRect)funcrec.getSubRects()[j]).setTextBox(v);
                        }
                    }
                }
                arr.addElement(funcrec, arr.getSubRects().length);
            }


        }


        base.setIndex(1, config);

        Main.mainPanel.leftPanel.addRect(base);

        //Main.preset = Preset.getPreset("ea");
        //FileManager.writeJSONToFile(FileManager.createSave(), path);
        //Main.cacheManager.addToBuffer("filesOpened", path);
        //InputHandler.actionHandler.saved();

        //if (!Main.nogui) {
        //    FileManager.loadSave(FileManager.readJSONFileToJSON(path));
        //}
    }
}
