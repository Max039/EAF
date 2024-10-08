package eaf.executor;

import eaf.Main;
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




    static void writeEvoAlFiles() {
        Main.preset.generateFiles(getPathToProject(), Main.mainPanel.leftPanel);

        Main.mainPanel.revalidate();
        Main.mainFrame.repaint();
    }



    public static void createScript(String path, String build) {

        LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Trying to writing Shell-Script script at " + path);
        try {
            List<String> lines = new ArrayList<>();
            lines.add(getReplacementLines(build));

            Files.write(Paths.get(path), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPathToProject() {
        var r = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        Path fileFullPath = Paths.get(r);
        return fileFullPath.getParent().toAbsolutePath().toString();
    }

    public static void setOpenAndExports() {
        openAndExports = new ArrayList<>();
        LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Searching for Modules in plugins ...");
        for (var plugin : PluginManager.plugins.values()) {
            ArrayList<String> modules = ModuleFinder.getModules(plugin.path);
            LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " =============");
            LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + plugin.name + " modules :");
            for (String moduleName : modules) {
                LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + moduleName);
                for (var module : ClassLocator.evoalModules) {
                    openAndExports.add(module + "=" + moduleName);
                }
            }
        }


    }

    private static String getReplacementLines(String build) {
        LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " Creating Opens And Exports of EvoAl Module for Plugin Modules ...");
        LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " EvoAl modules :");
        for (var module : ClassLocator.evoalModules) {
            LogManager.println(LogManager.scriptWriter() + LogManager.script() + LogManager.shell() + " " + module);
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

        if (!s.isEmpty()) {
            s += " -Dfile.encoding=utf-8";
        }
        else {
            s = "-Dfile.encoding=utf-8";
        }

        String currentDir = System.getProperty("user.dir");

        return "#!/bin/sh\n" +
                "export DISPLAY=localhost:0.0\n" +
                debugLine() +
                "export EVOAL_HOME=$( cd -- \"" + currentDir + "/" + Main.evoalBuildFolder + "/" + build + "/evoal\" >/dev/null 2>&1 ; pwd -P )\n" +
                "# Print the current working directory for debugging\n" +
                "echo \"Current working directory in second script: $(pwd)\"\n" +
                "echo \"EVOAL_HOME in second script: $EVOAL_HOME\"\n" +
                "export EVOAL_JVM_ARGUMENTS=\"" + s + "\"\n" +
                "# Get the directory of the currently executed script\n" +
                (Main.os == Main.OS.WINDOWS ? "SCRIPT_DIR=$(dirname \"$(readlink -f \"$0\")\")\n" : "SCRIPT_DIR=$(cd \"$(dirname \"$0\")\" && pwd)\n") +
                "# Change the working directory to the script's directory\n" +
                "cd \"$SCRIPT_DIR\"\n" +
                getExecutionLine();
    }

    private static String debugLine() {
        if (Main.debug) {
            return "export EVOAL_DEBUG=true";
        } else {
            return "";
        }
    }

    private static String getExecutionLine() {
       return Main.preset.executionLine();
    }

}