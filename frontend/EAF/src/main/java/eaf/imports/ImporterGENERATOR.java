package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.Module;
import eaf.rects.OptionsFieldRect;
import eaf.rects.RectFactory;
import eaf.rects.multi.ArrayRect;
import eaf.rects.multi.ClassRect;

import java.io.File;
import java.util.ArrayList;

import static eaf.Main.savesPath;

public class ImporterGENERATOR extends Importer {

    public void importFile(File file, String path) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];

        loadDDLFiles(file);

        LogManager.println(LogManager.importer() + LogManager.file() + " " +path);

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        content = SyntaxTree.removeComments(content);

        var base = (ClassRect) RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.generator.generator"));
        var piperec = (ClassRect) ((ArrayRect) base.getSubRectByName("pipelines")).getSubRects()[0];
        var steps = (ArrayRect) piperec.getSubRectByName("steps");
        steps.removeLast();



        var pipeline = SyntaxTree.extractBlock(content, "pipeline", '[', ']').trim();
        pipeline = pipeline.substring(1, pipeline.length() - 2);
        var parts = pipeline.split("step");
        for (var p : parts) {
            p = p.trim();
            if (!p.isEmpty()) {
                var step = (ClassRect) RectFactory.getRectFromClassType(SyntaxTree.get("de.eaf.generator.step"));
                var writearr = (ArrayRect) step.getSubRectByName("writes");
                writearr.removeLast();
                var readsarr = (ArrayRect) step.getSubRectByName("reads");
                readsarr.removeLast();

                p = p.substring(1, p.length() - 2);

                if (p.contains("writes")) {
                    var writes = SyntaxTree.extractBlock(p, "writes", '[', ']').replace("[", "").replace("]", "").replace("data", "").split(",");
                    for (var w : writes) {
                        var fill = (OptionsFieldRect) RectFactory.getRectFromFieldType(writearr.fillType, null);
                        writearr.addElement(fill, writearr.getSubRects().length);
                        fill.refreshComboBoxOptions();
                        fill.comboBox.setSelectedItem(w.trim());
                        fill.refreshComboBoxOptions();
                    }
                }

                if (p.contains("reads")) {
                    var reads = SyntaxTree.extractBlock(p, "reads", '[', ']').replace("[", "").replace("]", "").replace("data", "").split(",");
                    for (var r : reads) {
                        var fill = (OptionsFieldRect) RectFactory.getRectFromFieldType(readsarr.fillType, null);
                        readsarr.addElement(fill, readsarr.getSubRects().length);
                        fill.refreshComboBoxOptions();
                        fill.comboBox.setSelectedItem(r.trim());
                        fill.refreshComboBoxOptions();
                    }
                }

                p = SyntaxTree.removeSubstring(p, "writes", ";");
                p = SyntaxTree.removeSubstring(p, "reads", ";");
                p = p.trim();
                
                var pparts = p.split("\\{", 2);
                var head = pparts[0].replace("component", "").trim();
                var body = pparts[1].replace("}", "").trim();
                var prob = SyntaxTree.processContentOfType(tempModule.resolveClass(head).instance(), body, tempModule);
                step.setIndex(0, RectFactory.getRectFromClassType(prob.instance));

                steps.addElement(step, steps.getSubRects().length);

            }


        }



        Main.mainPanel.leftPanel.addRect(base);
        //Main.preset = Preset.getPreset("generator");
        //FileManager.writeJSONToFile(FileManager.createSave(), path);
        //Main.cacheManager.addToBuffer("filesOpened", path);
        //InputHandler.actionHandler.saved();

        //if (!Main.nogui) {
        //    FileManager.loadSave(FileManager.readJSONFileToJSON(path));
        //}
    }

}
