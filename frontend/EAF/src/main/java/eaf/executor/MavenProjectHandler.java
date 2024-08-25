package eaf.executor;

import eaf.Main;
import eaf.plugin.PluginManager;
import eaf.ui.panels.ConsolePane;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MavenProjectHandler {

    public static void handleMavenProject(String projectPath, String destinationPath, String name) {
        try {
            // Step 1: Clean, install, and package the Maven project
            runMavenCommand(projectPath, List.of("clean", "install", "package"));

            // Step 2: Copy the JAR file to the given destination path
            copyJarFile(projectPath, destinationPath, name);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runMavenCommand(String projectPath, List<String> commands) throws IOException, InterruptedException {
        for (var command : commands) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(projectPath));
            processBuilder.command("mvn.cmd", command);
            Process process = processBuilder.start();

            // Capture the combined output (stdout and stderr)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while (process.isAlive() || reader.ready()) {
                while ((reader.ready() && (line = reader.readLine()) != null)) {
                    System.out.println(line);
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
                Main.console.println("Maven command failed with exit code " + exitCode);
                throw new RuntimeException("Maven command failed with exit code " + exitCode);
            }
        }
    }

    public static void copyJarFile(String projectPath, String destinationPath, String newFileName) throws IOException {
        Path targetDir = Paths.get(projectPath, "target/plugin");
        if (!Files.exists(targetDir)) {
            throw new RuntimeException("Target directory does not exist.");
        }

        // Find the JAR file in the target directory
        File[] jarFiles = targetDir.toFile().listFiles((dir, name) -> name.startsWith("plugin"));
        if (jarFiles == null || jarFiles.length == 0) {
            throw new RuntimeException("No JAR file found in the target directory.");
        }

        System.out.println(jarFiles[0].getPath());
        // Assume the first JAR file is the one we want to copy (this can be customized)
        File jarFile = jarFiles[0];

        // Determine the destination file path, using the new file name
        Path destination = Paths.get(destinationPath, newFileName);

        // Copy the JAR file to the destination path, replacing it if it already exists
        Files.copy(jarFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Copied " + jarFile.getName() + " to " + destinationPath + " as " + newFileName);
    }

    public static void copyPlugins() {
        String currentPath = System.getProperty("user.dir");
        for (var plugin : PluginManager.plugins) {
            ArrayList<String> ignore = new ArrayList<>();
            ignore.add(plugin.path + "/target");
            ignore.add(plugin.path + "/.idea");
            File f = new File(currentPath + "/EvoAlBuilds/" + Main.evoalVersion + "/evoal/plugins/"+ plugin.name + ".jar");
            System.out.println(f.getPath());
            if (FileChangesChecker.updateFileJson(plugin.path, ignore) || !f.exists()) {
                if (f.exists()) {
                    f.delete();
                }
                Main.console.println("Recompiling and Copying Plugin " + plugin.name + " ...");
                handleMavenProject(plugin.path, currentPath + "/EvoAlBuilds/" + Main.evoalVersion + "/evoal/plugins", plugin.name + ".jar");
                Main.console.flush();
            }
        }
    }


}

