package eaf.executor;

import eaf.manager.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class OpenIntelliJProject {


    static {
        findNewestIdea64();
    }

    public static String ideaPath;


    public static void findNewestIdea64() {

        // Path to the Program Files directory
        String programFilesPath = System.getenv("ProgramFiles");
        if (programFilesPath == null) {
            programFilesPath = "C:\\Program Files";
        }

        // Directory where IntelliJ IDEA is usually installed
        String intelliJPath = programFilesPath + "\\JetBrains";

        // Create a file object for this directory
        File intelliJDir = new File(intelliJPath);

        if (intelliJDir.exists() && intelliJDir.isDirectory()) {
            // Get all directories that start with "IntelliJ IDEA"
            File[] ideaDirs = intelliJDir.listFiles((dir, name) -> name.startsWith("IntelliJ IDEA"));

            if (ideaDirs != null && ideaDirs.length > 0) {
                // Sort the directories by name in reverse order (newest first)
                Arrays.sort(ideaDirs, Comparator.comparing(File::getName).reversed());

                // Assume the first directory is the newest version
                File newestVersionDir = ideaDirs[0];

                // Path to the idea64.exe file
                String ideaExePath = newestVersionDir.getAbsolutePath() + "\\bin\\idea64.exe";

                // Check if the file exists
                File ideaExeFile = new File(ideaExePath);
                if (ideaExeFile.exists()) {
                    System.out.println(LogManager.intellij() + " Path to the newest version of IntelliJ IDEA: " + ideaExePath);
                    ideaPath = ideaExePath;
                } else {
                    System.out.println(LogManager.intellij() + LogManager.error() + " idea64.exe not found in the newest version directory.");
                }
            } else {
                System.out.println(LogManager.intellij() + LogManager.error() + " No IntelliJ IDEA installations found.");
            }
        } else {
            System.out.println(LogManager.intellij() + LogManager.error() + " JetBrains directory not found.");
        }

    }



    public static void openProject(String project) {
        openFile(project, "");
    }

    public static void openFile(String project, String file) {

        if (!ideaPath.isEmpty()) {
            List<String> command = new ArrayList<>();
            // Create a ProcessBuilder to open the project
            ProcessBuilder processBuilder = new ProcessBuilder(ideaPath, project + file);

            // Start the process
            try {
                Process process = processBuilder.start();
                System.out.println(LogManager.intellij() + " IntelliJ IDEA launched successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println(LogManager.intellij() + LogManager.warning() + " Cannot open project no IntelliJ IDEA found!");
        }


    }
}