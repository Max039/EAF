package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.Module;
import eaf.rects.RectFactory;
import eaf.setup.Preset;

import java.io.File;
import java.util.ArrayList;

import static eaf.Main.savesPath;

public class ImporterMLL extends  Importer {

    public void importFile(File file, String path) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        content = SyntaxTree.removeComments(content);

        for (var function : SyntaxTree.getFormatParts(content, "function", "function", "for")) {
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
            Main.mainPanel.leftPanel.addRect(rec);
            LogManager.println("============");
        }


        //loadDDLFiles(file);

        //FileManager.copyFilesWithEndings(file.getParentFile().getAbsolutePath(), curr + savesPath + "/" + filename, ".csv", ".pson", ".xlsx");

        //Main.preset = Preset.getPreset("ea");
        //FileManager.writeJSONToFile(FileManager.createSave(), path);
        //Main.cacheManager.addToBuffer("filesOpened", path);
        //InputHandler.actionHandler.saved();
    }
}
