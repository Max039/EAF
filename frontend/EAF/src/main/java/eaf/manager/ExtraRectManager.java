package eaf.manager;

import eaf.compiler.ClassFactory;
import eaf.compiler.SyntaxTree;
import eaf.models.ClassType;
import eaf.models.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        List<File> filesToProcess = new ArrayList<>();

        // Recursively collect all .rect files
        collectRectFiles(directory, filesToProcess);

        // Keep track of whether we processed any files in the last full iteration
        boolean processedFile;

        do {
            processedFile = false;
            Iterator<File> iterator = filesToProcess.iterator();

            while (iterator.hasNext()) {
                File file = iterator.next();
                String filePath = file.getPath();
                System.out.print("Trying to process: " + filePath);

                // Generate the package name by removing the base path and modifying the rest
                String pack = filePath
                        .replace("\\", "/")
                        .replace(System.getProperty("user.dir").replace("\\", "/") + pathToRects + "/", "")
                        .replace(extension, "")
                        .replace("/", ".");

                Pair<Boolean, ClassType> res;
                try {
                    // Create the ClassType object from the .rect file
                    res = ClassFactory.fromRectFile(FileManager.readJSONFileToJSON(file), pack);

                    // Check if the processing was successful
                    if (res.getFirst()) {

                        var c = res.getSecond();



                        if (SyntaxTree.get(c.name) != null) {
                            System.out.println(" -> processing " + ColorManager.colorText("failed", ColorManager.errorColor) + " rect file name \"" + c.name + "\" already taken!");
                        }
                        else {
                            System.out.println(" -> "  + ColorManager.colorText("successfully", ColorManager.sucessColor) +  " created \"" + c.name + "\"");
                            processedFile = true;
                            iterator.remove(); // Remove the successfully processed file from the list
                            // Register the ClassType object
                            classRegister.put(c.name, c);
                            if (c.parent == null) {
                                baseClassRegister.put(c.name, c);
                            }

                            if (c.parent != null) {
                                c.parent.addChild(c);
                            }
                        }

                    } else {
                        System.out.println(" -> processing " + ColorManager.colorText("failed", ColorManager.errorColor) + " parent not found!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } while (processedFile && !filesToProcess.isEmpty());

        System.out.println("============================");
        if (filesToProcess.isEmpty()) {
            System.out.println(ColorManager.colorText("Successfully", ColorManager.sucessColor) + " loaded all .rect files!");
        }
        else {
            for (var leftFiles : filesToProcess) {
                System.out.println(LogManager.error() + " Could not load file: " + leftFiles.getPath());
            }
        }

    }

    // Helper method to recursively collect .rect files
    private static void collectRectFiles(File directory, List<File> filesToProcess) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectRectFiles(file, filesToProcess);
            } else if (file.isFile() && file.getName().endsWith(extension)) {
                filesToProcess.add(file);
            }
        }
    }
    public static void start() {
        loadRects();
    }


}
