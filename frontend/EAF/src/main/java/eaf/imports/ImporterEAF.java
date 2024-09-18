package eaf.imports;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;

import java.io.File;

import static eaf.Main.savesPath;
import static eaf.manager.FileManager.loadSave;
import static eaf.manager.FileManager.readJSONFileToJSON;

public class ImporterEAF extends Importer{
    public void importFile(File file, String path) throws Exception {
        String curr = System.getProperty("user.dir");
        String name = file.getName().split("\\.")[0];

        var newFile = FileManager.copyFile(file.getAbsolutePath(), path);
        loadSave(readJSONFileToJSON(newFile));
        Main.cacheManager.addToBuffer("filesOpened", newFile.getPath());
    };
}
