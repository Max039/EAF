package eaf.manager;

import eaf.compiler.ClassFactory;
import eaf.models.ClassType;

import java.io.File;
import java.util.HashMap;

import static eaf.models.ClassType.getClassHierarchy;

public class ExtraRectManager {
    static String pathToRects = "/rects";
    static String extension = ".rect";

    public static HashMap<String, ClassType> baseClassRegister;

    public static HashMap<String, ClassType> classRegister;

    public static void saveRect(ClassType c) {
        var content = ClassFactory.toRectFile(c);
        String rootDirectory = System.getProperty("user.dir") + pathToRects + "/" + c.pack.replace(".", "/") + "/" + c.name + extension;
        FileManager.writeJSONToFile(content, rootDirectory);
    }

    public static void loadRects() {
        classRegister = new HashMap<>();
        baseClassRegister  = new HashMap<>();
        // Get the root directory for .rect files
        File rootDirectory = new File(System.getProperty("user.dir") + pathToRects);

        // Recursively find and process all .rect files
        loadRectFiles(rootDirectory);
    }

    private static void loadRectFiles(File directory) {
        // List all files and directories in the current directory
        File[] files = directory.listFiles();
        if (files == null) return; // If directory is empty or an I/O error occurs, exit

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively process subdirectories
                loadRectFiles(file);
            } else if (file.isFile() && file.getName().endsWith(extension)) {
                // Process .rect files
                String filePath = file.getPath();
                System.out.print("Processing: " + filePath);
                // Generate the package name by removing the base path and modifying the rest
                String pack = filePath
                        .replace("\\", "/")
                        .replace(System.getProperty("user.dir").replace("\\", "/") + pathToRects + "/", "")
                        .replace(extension, "")
                        .replace("/", ".");

                ClassType c;
                try {
                    // Create the ClassType object from the .rect file
                    c = ClassFactory.fromRectFile(FileManager.readJSONFileToJSON(file), pack);
                    System.out.println(" -> created \"" + c.name + "\"");
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Register the ClassType object
                classRegister.put(c.name, c);
                if (c.parent == null) {
                    baseClassRegister.put(c.name, c);
                }
            }
        }
    }

    public static void start() {
        loadRects();
    }
}
