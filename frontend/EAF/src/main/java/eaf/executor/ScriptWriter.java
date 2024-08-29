package eaf.executor;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.plugin.PluginManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ScriptWriter {

    public static ArrayList<String> openAndExports;

    public static ArrayList<String> evoAlModules;

    public static String pathToModuleFile = "/evoal.modules";


    static void saveAndWriteEvoAlFiles() {
        System.out.println(LogManager.scriptWriter() + LogManager.write() + " Saving project ...");
        FileManager.save();
        System.out.println(LogManager.scriptWriter() + LogManager.write() + LogManager.data() + " Writing EvoAl Data ...");
        FileManager.write(Main.dataPanel.toString(), getPathToProject() + "/config.ddl");
        System.out.println(LogManager.scriptWriter() + LogManager.write() + LogManager.ol()  + " Writing EvoAl Script ...");
        FileManager.write(Main.mainPanel.leftPanel.toString(), getPathToProject()+ "/config.ol");
        Main.mainPanel.revalidate();
        Main.mainFrame.repaint();
    }

    public enum ScriptType {
        SEARCH
    }

    public static void getEvoAlModules() {
        String currentPath = System.getProperty("user.dir");
        evoAlModules = new ArrayList<>();
        try {
            // Read all lines from the file
            for (String line : Files.readAllLines(Paths.get(currentPath + pathToModuleFile))) {
                // Trim the line to remove leading and trailing whitespace
                String trimmedLine = line.trim();

                // Add to the list if it's not empty
                if (!trimmedLine.isEmpty()) {
                    evoAlModules.add(trimmedLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ScriptType projectType = ScriptType.SEARCH;

    public static void createScript(String path, String build, ScriptType type) {

        System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Trying to writing Shell-Script script at " + path);
        try {
            List<String> lines = new ArrayList<>();
            lines.add(getReplacementLines(build, type));

            Files.write(Paths.get(path), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPathToProject() {
        var r = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        Path fileFullPath = Paths.get(r);
        return Main.savesPath.substring(1) + "/" + fileFullPath.getParent().getFileName();

    }

    public static void setOpenAndExports() {
        openAndExports = new ArrayList<>();
        System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Searching for Modules in plugins ...");
        for (var plugin : PluginManager.plugins.values()) {
            ArrayList<String> modules = ModuleFinder.getModules(plugin.path);
            System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + plugin.name + " modules :");
            for (String moduleName : modules) {
                System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + moduleName);
                for (var module : evoAlModules) {
                    openAndExports.add(module + "=" + moduleName);
                }
            }
        }


    }

    private static String getReplacementLines(String build, ScriptType type) {
        System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Creating Opens And Exports of EvoAl Module for Plugin Modules ...");
        System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " EvoAl modules :");
        for (var module : evoAlModules) {
            System.out.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + module);
        }
        setOpenAndExports();
        String s = "";
        for (String i : openAndExports) {
            String export = "--add-exports=" + i;
            String opens = "--add-opens=" + i;
            if (!s.isEmpty()) {
                s += " " + opens + " " + export;
            }
            else {
                s = opens + " " + export;
            }
        }


        return "#!/bin/sh\n" +
                "export DISPLAY=localhost:0.0\n" +
                "export EVOAL_HOME=$( cd -- \"$(dirname $0)/../../EvoAlBuilds/" + build + "/evoal\" >/dev/null 2>&1 ; pwd -P )\n" +
                "# Print the current working directory for debugging\n" +
                "echo \"Current working directory in second script: $(pwd)\"\n" +
                "echo \"EVOAL_HOME in second script: $EVOAL_HOME\"\n" +
                "export EVOAL_JVM_ARGUMENTS=\"" + s + "\"\n" +
                "# Get the directory of the currently executed script\n" +
                "SCRIPT_DIR=$(dirname \"$(readlink -f \"$0\")\")\n" +
                "# Change the working directory to the script's directory\n" +
                "cd \"$SCRIPT_DIR\"\n" +
                getExecutionLine(type);
    }

    private static String getExecutionLine(ScriptType type) {
        switch (type) {
            case SEARCH -> {
                return  "$SHELL $EVOAL_HOME/bin/evoal-search.sh . config.ol output";
            }
            default ->
            {
                return "";
            }
        }
    }

}