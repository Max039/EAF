package eaf.imports;

import eaf.compiler.SyntaxTree;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.Module;

import java.io.File;
import java.util.ArrayList;

import static eaf.Main.savesPath;

public class ImporterGENERATOR extends Importer {

    public void importFile(File file, String path) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];

        LogManager.println(LogManager.importer() + LogManager.file() + " " +path);

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        content = SyntaxTree.removeComments(content);




        //loadDDLFiles(file);

        //FileManager.copyFilesWithEndings(file.getParentFile().getAbsolutePath(), curr + savesPath + "/" + filename, ".csv", ".pson", ".xlsx");

        //Main.preset = Preset.getPreset("ea");
        //FileManager.writeJSONToFile(FileManager.createSave(), path);
        //Main.cacheManager.addToBuffer("filesOpened", path);
        //InputHandler.actionHandler.saved();
    }

}
