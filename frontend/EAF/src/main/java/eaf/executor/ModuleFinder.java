package eaf.executor;

import eaf.manager.LogManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ModuleFinder {

    public static ArrayList<String> getModules(String projectRootPath) {
        ArrayList<String> modules = new ArrayList<>();
        File projectRoot = new File(projectRootPath);
        if (projectRoot.exists() && projectRoot.isDirectory()) {
            findModules(projectRoot, modules);
        } else {
            LogManager.println("Invalid project root path provided.");
        }
        return modules;
    }

    private static void findModules(File directory, ArrayList<String> modules) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findModules(file, modules);  // Recurse into subdirectories
                } else if (file.getName().equals("module-info.java")) {
                    String moduleName = extractModuleName(file);
                    if (moduleName != null) {
                        modules.add(moduleName);
                    }
                }
            }
        }
    }

    private static String extractModuleName(File moduleInfoFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(moduleInfoFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("module ")) {
                    return line.split(" ")[1].replace(";", "").trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + moduleInfoFile.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }


}