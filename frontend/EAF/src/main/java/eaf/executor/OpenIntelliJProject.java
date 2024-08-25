package eaf.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class OpenIntelliJProject {

    public static String ideaPath = "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2021.3.2\\bin\\idea64.exe"; // Adjust according to your system

    // Path to the project to open
    public static String projectPath = "C:\\Users\\mpies\\Documents\\GitHub\\EAF\\frontend\\EAF\\plugins\\eaf"; // Adjust according to your project path

    public static String classToLoad = "/src/main/java/de/eaf/statistics/Hook.java"; // Adjust according to your class

    public static void main(String[] args) {
        openProject(projectPath);

    }

    public static void openProject(String project) {
        openFile(project, "");
    }

    public static void openFile(String project, String file) {

        List<String> command = new ArrayList<>();
        // Create a ProcessBuilder to open the project
        ProcessBuilder processBuilder = new ProcessBuilder(ideaPath, project + file);

        // Start the process
        try {
            Process process = processBuilder.start();
            System.out.println("IntelliJ IDEA launched successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}