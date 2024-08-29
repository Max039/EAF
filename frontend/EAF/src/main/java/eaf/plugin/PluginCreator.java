package eaf.plugin;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.executor.ClassLocator;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.models.Pair;
import eaf.ui.UiUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class PluginCreator {
    public static HashMap<String, Pair<String, ClassType>> examples;

    public static String pathToExamples = "/class_examples";

    public static Plugin currentPlugin = null;

    public static void main(String[] args) throws IOException {
        SyntaxTree.start();
        loadExamples();

        currentPlugin = createBaseForPlugin("test", "de.test");
        createNewFromExample();
    }

    public static void createNewFromExample() {
        if (currentPlugin != null) {
            var o = UiUtil.selectClassType(getCreateAbleClasses());
            if (o != null) {
                openClassMenuAndAddToPlugin(currentPlugin.path, "test", (ClassType) o);
            }
        }
       else {
            noPluginSelected();
        }
    }

    public static void noPluginSelected() {
        System.out.println(LogManager.plugin() + LogManager.error() + " No plugin selected!");
        showMessageDialog(Main.mainFrame, "No selected plugin!", "Error", ERROR_MESSAGE);
    }



    public static List<ClassType> getExampleClasses() {
        return examples.values().stream().map(Pair::getSecond).toList();
    }

    public static List<ClassType> getCreateAbleClasses() {
        ArrayList<ClassType> l = new ArrayList<>();
        for (var e : getExampleClasses()) {
            l.add(e);
            l.addAll(e.getAllDescendants());
        }
        return l;
   }


    private static void findJavaFilesRecursively(File folder, HashMap<String, Pair<String, ClassType>> javaFiles) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                findJavaFilesRecursively(file, javaFiles); // Recursively search in subfolders
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                var name = file.getName().replace(".java", "");
                var found =  SyntaxTree.get(name);
                if (found != null) {
                    javaFiles.put(file.getName(), new Pair<>(file.getAbsolutePath(), found)); // Add the .java file path to the list
                }
                else {
                    System.out.println(LogManager.plugin() + LogManager.error() + " Name of example class " + name + " not found in Class Register!");
                }

            }
        }
    }


    public static void loadExamples() {
        examples = new HashMap<>();
        String currentDirectory = System.getProperty("user.dir");
        File folder = new File(currentDirectory + pathToExamples);

        if (folder.exists() && folder.isDirectory()) {
            findJavaFilesRecursively(folder, examples);
        }
    }

    public static HashMap<String, Pair<String, ClassType>> getExamples() {
        return examples;
    }

    public static Plugin createBaseForPlugin(String pluginName, String moduleName) {
        ClassLocator.refreshEvoAlModules();
        String currentDirectory = System.getProperty("user.dir");
        try {
            FileManager.copyFolder(currentDirectory + "/plugin_base", currentDirectory + "/plugins/" + pluginName);
            ArrayList<Pair<String, String>> pomReplacements = new ArrayList<>();
            pomReplacements.add(new Pair<>("#name#", pluginName));
            pomReplacements.add(new Pair<>("#module#", moduleName));

            var exportsAndOpens = "";
            for (var i : ClassLocator.evoalModules) {
                if (exportsAndOpens.isEmpty()) {
                    exportsAndOpens += "\n";
                }
                exportsAndOpens += "\t\t\t";
                exportsAndOpens += "<arg>--add-exports=" + i + "=" + moduleName + "</arg>";
                exportsAndOpens += "\n";
                exportsAndOpens += "\t\t\t";
                exportsAndOpens += "<arg>--add-opens=" + i + "=" + moduleName + "</arg>";
                exportsAndOpens += "\n";
            }

            pomReplacements.add(new Pair<>("#OpenAndExportmodules#", exportsAndOpens));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/pom.xml" , pomReplacements);


            ArrayList<Pair<String, String>> moduleReplacements = new ArrayList<>();
            moduleReplacements.add(new Pair<>("#module#", moduleName));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/src/main/java/module-info.java" , moduleReplacements);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Main.pluginManager.discoverPlugins();
        return PluginManager.plugins.get(pluginName);
    }

    public static void openClassMenuAndAddToPlugin(String pluginFolder, String name, ClassType parent) {
        if (currentPlugin != null) {
            addClass(pluginFolder, name, UiUtil.openClassEditorAndReturn(parent, true, false));
            System.out.println("test");
        }
        else {
            noPluginSelected();
        }

    }

    public static void addClass(String pluginFolder, String name, ClassType type) {
        String pack = type.pack;
        //String module = type.toModule();
    }



}
