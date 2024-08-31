package eaf.executor;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.plugin.PluginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MavenProjectHandler {

    public static void handleMavenProject(String projectPath, String destinationPath, String name) {
        try {
            // Step 1: Clean, install, and package the Maven project
            runMavenCommand(projectPath, List.of("clean", "install", "package"));

            // Step 2: Copy the JAR file to the given destination path
            FileManager.copyJarFile(projectPath, destinationPath, name);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runMavenCommand(String projectPath, List<String> commands) throws IOException, InterruptedException {
        for (var command : commands) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            //processBuilder.directory(new File(projectPath));
            switch (Main.os) {
                case MAC -> {
                    processBuilder.command("cd", projectPath, "&&", "mvn", command);
                }
                case WINDOWS -> {
                    processBuilder.command("mvn.cmd", command);
                }
            }

            Process process = processBuilder.start();

            // Capture the combined output (stdout and stderr)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while (process.isAlive() || reader.ready()) {
                while ((reader.ready() && (line = reader.readLine()) != null)) {
                    System.out.println(LogManager.maven() + " " + line);
                    Main.console.println(line);

                }
                try {
                    Thread.sleep(50);
                }
                catch (Exception e) {

                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Main.console.println(LogManager.maven() + LogManager.error() + " Maven command failed with exit code " + exitCode);
                throw new RuntimeException("Maven command failed with exit code " + exitCode);
            }
        }
    }

    public static void copyPlugins() {
        String currentPath = System.getProperty("user.dir");
        for (var plugin : PluginManager.plugins.values()) {
            ArrayList<String> ignore = new ArrayList<>();
            ignore.add(plugin.path + "/target");
            ignore.add(plugin.path + "/.idea");
            File f = new File(currentPath + "/" + Main.evoalBuildFolder + "/" + Main.evoalVersion + "/evoal/plugins/"+ plugin.name + ".jar");
            System.out.println(LogManager.maven() + " Preparing Plugin " + plugin.name + " at " + f.getPath());
            if (FileChangesChecker.updateFileJson(plugin.path, ignore) || !f.exists()) {
                if (f.exists()) {
                    f.delete();
                }
                Main.console.println("Recompiling and Copying Plugin " + plugin.name + " ...");
                handleMavenProject(plugin.path, currentPath + "/" + Main.evoalBuildFolder + "/" + Main.evoalVersion + "/evoal/plugins", plugin.name + ".jar");
                Main.console.flush();
            }
        }
    }


}

