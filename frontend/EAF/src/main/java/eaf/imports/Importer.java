package eaf.imports;


import java.io.File;
import java.util.HashMap;

public abstract class Importer {

    public static HashMap<String, Importer> importer;
    public static void prepareImporter() {
        importer = new HashMap<>();
        importer.put("eaf", new ImporterEAF());
        importer.put("ddl", new ImporterDDL());
        importer.put("ol", new ImporterOL());
        importer.put("mll", new ImporterMLL());
        importer.put("generator", new ImporterGENERATOR());
    }

    public abstract void importFile(File file, String target) throws Exception;

    public static void loadDDLFiles(File in) {
        // Create a File object for the parent directory
        File folder = in.getParentFile();
        // Check if the folder exists and is a directory
        if (folder.exists() && folder.isDirectory()) {
            // List all files in the directory
            File[] files = folder.listFiles();

            if (files != null) {
                // Loop through the files and print those ending with '.ddl'
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".ddl")) {
                        try {
                            importer.get("ddl").importFile(file, "");
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

}
