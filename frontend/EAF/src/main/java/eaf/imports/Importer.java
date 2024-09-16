package eaf.imports;


import java.io.File;
import java.util.HashMap;

public abstract class Importer {

    public static HashMap<String, Importer> importer;
    public static void prepareImporter() {
        importer = new HashMap<>();
        importer.put("eaf", new ImporterEAF());
        importer.put("ddl", new ImporterDDL());
    }

    public abstract void importFile(File file) throws Exception;
}
