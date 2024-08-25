package eaf.executor;

import eaf.Main;
import eaf.plugin.Plugin;
import eaf.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
            processBuilder.inheritIO();  // Optional: to inherit I/O to see the Maven output
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
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
            handleMavenProject(plugin.path, currentPath + "/EvoAlBuilds/" + Main.evoalVersion + "/evoal/plugins", plugin.name + ".jar");
        }
    }


}

