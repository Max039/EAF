package eaf.imports;

import eaf.Main;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;

import java.io.File;

import static eaf.Main.savesPath;

public class ImporterDDL extends Importer{

    public void importFile(File file) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String name = file.getName().split("\\.")[0];
        String path = curr + "/" + savesPath + "/" + name + "/" + name + ".eaf";
        LogManager.println(path);





        
        FileManager.writeJSONToFile(FileManager.createSave(), path);
        Main.cacheManager.addToBuffer("filesOpened", path);
        InputHandler.actionHandler.saved();
    }
}
