package eaf.imports;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;

import java.io.File;

import static eaf.Main.savesPath;
import static eaf.manager.FileManager.loadSave;
import static eaf.manager.FileManager.readJSONFileToJSON;

public class ImporterEAF extends Importer{
    public void importFile(File file) throws Exception {
        String curr = System.getProperty("user.dir");
        String name = file.getName().split("\\.")[0];
        LogManager.println(curr + "/" + savesPath + "/" + name + "/" + file.getName());
        var newFile = FileManager.copyFile(file.getAbsolutePath(), curr + savesPath + "/" + name + "/" + file.getName());
        FileManager.copyFolder(curr + "/project_base", curr + "/" + savesPath + "/" + name);
        loadSave(readJSONFileToJSON(newFile));
        Main.cacheManager.addToBuffer("filesOpened", newFile.getPath());
    };
}
