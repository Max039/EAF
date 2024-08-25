package eaf.executor;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ScriptWriter {

    public static ArrayList<String> openAndExports;

    static {
        openAndExports  = new ArrayList<>();
        openAndExports.add("de.evoal.optimisation.api/de.evoal.optimisation.api.statistics=de.evoal.eaf");
    }

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

    private static String getReplacementLines(String build, ScriptType type) {
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