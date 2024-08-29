package eaf.executor;

import eaf.Main;
import eaf.manager.ColorManager;
import eaf.manager.LogManager;
import eaf.models.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLocator {

    public static String pathToModuleFile = "/evoal.requirements";

    public static  ArrayList<String> classNames = new ArrayList<>();
    public static  ArrayList<String> evoalModules;
    public static void refreshEvoAlModules() {
        String currentDirectory = System.getProperty("user.dir");
        // Example usage
        ArrayList<String> folderPaths = new ArrayList<>();
        folderPaths.add(currentDirectory + "/EvoAlBuilds/" + Main.evoalVersion + "/evoal/modules");
        folderPaths.add(currentDirectory + "/EvoAlBuilds/" + Main.evoalVersion + "/evoal/plugins");

        refreshClassNames();



        evoalModules = new ArrayList<>();
        try {
            System.out.println(LogManager.classLocator() + " Searching required classes in EvoAl files...");

            HashMap<String, Pair<Class<?>, String>> classMap = loadClassesFromJars(folderPaths, classNames);


            System.out.println(LogManager.classLocator() + " =============");
            System.out.println(LogManager.classLocator() + " Preparing final module list ...");


            // Print the loaded classes with filtered class names
            var filteredClassNames = filterClassNames(classMap);
            for (var entry : filteredClassNames) {
                Class<?> clazz = entry.getValue().getFirst();
                String moduleName = entry.getValue().getSecond();

                var module = moduleName + "/" + clazz.getPackageName();
                evoalModules.add(module);
                System.out.println(LogManager.classLocator() + " " + module);

            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void refreshClassNames() {
        String currentPath = System.getProperty("user.dir");
        classNames = new ArrayList<>();
        try {
            // Read all lines from the file
            for (String line : Files.readAllLines(Paths.get(currentPath + pathToModuleFile))) {
                // Trim the line to remove leading and trailing whitespace
                String trimmedLine = line.trim();

                // Add to the list if it's not empty
                if (!trimmedLine.isEmpty()) {
                    classNames.add(trimmedLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<Map.Entry<String, Pair<Class<?>, String>>> filterClassNames(HashMap<String, Pair<Class<?>, String>> classMap) {
        // Step 1: Extract unique package prefixes
        Set<Map.Entry<String, Pair<Class<?>, String>>> packages = classMap.entrySet();
        Set<Map.Entry<String, Pair<Class<?>, String>>> majorPackages = new HashSet<>();

        // Collect all possible package prefixes
        for (var pkg : packages) {
            majorPackages.add(pkg);
        }

        Map<String, Pair<Class<?>, String>> uniquePackages = new HashMap<>();
        for (var i : majorPackages) {
            String pkg = i.getValue().getFirst().getPackageName();
            // Check if the package is already in the map
            boolean isNested = false;
            for (String existingPkg : uniquePackages.keySet()) {
                if (pkg.startsWith(existingPkg + ".") && !pkg.equals(existingPkg)) {
                    // The current package is nested within an existing one
                    isNested = true;
                    break;
                }
            }
            if (!isNested) {
                uniquePackages.put(pkg, i.getValue());
            }
        }
        return new HashSet<>(uniquePackages.entrySet());
    }

    public static HashMap<String, Pair<Class<?>, String>> loadClassesFromJars(ArrayList<String> folderPaths, ArrayList<String> classNames)
            throws IOException, ClassNotFoundException {
        ArrayList<URL> jarUrls = new ArrayList<>();

        for (String folderPath : folderPaths) {
            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                // Recursively find and add all JAR files in the folder to the list
                addJarFilesFromFolder(folder, jarUrls);
            }
        }

        // Create a URLClassLoader with all JAR URLs
        URLClassLoader classLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());

        // Load classes using the combined class loader
        HashMap<String, Pair<Class<?>, String>> classMap = new HashMap<>();
        for (URL jarUrl : jarUrls) {
            File jarFile = new File(jarUrl.getFile());
            processJarFile(jarFile, classNames, classMap, classLoader);
        }

        for (var a : classNames) {
            if (classMap.get(a) == null) {
                System.out.println(LogManager.classLocator() + LogManager.error() + " Found " + ColorManager.colorText("NO", ColorManager.errorColor) + " Class for: " + a);
            }
        }

        return classMap;
    }

    private static void addJarFilesFromFolder(File folder, ArrayList<URL> jarUrls) throws IOException {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // If it's a directory, process it recursively
                    addJarFilesFromFolder(file, jarUrls);
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    // If it's a JAR file, add it to the list of URLs
                    jarUrls.add(file.toURI().toURL());
                }
            }
        }
    }

    private static void processJarFile(File jarFile, ArrayList<String> classNames, HashMap<String, Pair<Class<?>, String>> classMap, URLClassLoader classLoader)
            throws IOException, ClassNotFoundException {

        // Open the JAR file
        try (JarFile jar = new JarFile(jarFile)) {

            // Iterate over all entries in the JAR file
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Check if the entry is a class file
                if (name.endsWith(".class")) {
                    // Convert the class file path to a fully qualified class name
                    String className = name.replace('/', '.').replace(".class", "");

                    // Extract the simple class name
                    String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

                    // Check if the class name matches any of the specified names
                    if (classNames.contains(simpleClassName) && className.contains("evoal")) {
                        System.out.println(LogManager.classLocator() + " =============");
                        System.out.println(LogManager.classLocator() + " Class found: " + simpleClassName);
                        System.out.println(LogManager.classLocator() + " In jar: " + jarFile.getName() + " at " + jarFile.getAbsolutePath());
                        System.out.println(LogManager.classLocator() + " Class Path: " + className);

                        JarEntry moduleInfoEntry = jar.getJarEntry("module-info.class");

                        if (moduleInfoEntry == null) {
                            System.out.println(LogManager.classLocator() + LogManager.error() + " No module-info.class found in the JAR file.");
                            return;
                        }

                        final String[] moduleNames = {""};
                        try (InputStream is = jar.getInputStream(moduleInfoEntry)) {
                            ClassReader classReader = new ClassReader(is);
                            ClassVisitor moduleVisitor = new ClassVisitor(Opcodes.ASM9) {

                                @Override
                                public ModuleVisitor visitModule(String moduleName, int access, String version) {
                                    moduleNames[0] = moduleName;
                                    System.out.println(LogManager.classLocator() + " Module: " + moduleName);
                                    return super.visitModule(moduleName, access, version);
                                }
                            };
                            classReader.accept(moduleVisitor, 0);
                        } catch (Exception e) {
                            System.err.println("An error occurred while processing module-info.class: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // Load the class using the combined class loader
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            classMap.put(simpleClassName, new Pair<>(clazz, moduleNames[0]));
                        } catch (ClassNotFoundException e) {
                            System.err.println("Failed to load class: " + className);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



}
