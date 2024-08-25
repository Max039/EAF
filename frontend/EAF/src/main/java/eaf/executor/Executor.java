package eaf.executor;

import eaf.Main;
import eaf.input.InputHandler;
import eaf.manager.ColorManager;
import eaf.manager.LogManager;
import eaf.plugin.PluginManager;
import eaf.process.GenerationTracker;
import eaf.sound.SoundManager;
import eaf.ui.panels.ErrorPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Executor {

    public static void execute() throws IOException, InterruptedException {
        InputHandler.processStarted();

        MavenProjectHandler.copyPlugins();

        String currentPath = System.getProperty("user.dir");
        var scriptTarget = currentPath + "/" +  ScriptWriter.getPathToProject() + "/run.sh";

        System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Requesting script at " + scriptTarget + " .... ");
        ScriptWriter.createScript(scriptTarget, Main.evoalVersion, ScriptWriter.projectType);
        System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Trying to run script ...");
        try {

            String scriptPath = currentPath + "/" + ScriptWriter.getPathToProject() + "/run.sh";
            System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Current path: " + currentPath);
            System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Script path: " + scriptPath);
            String[] command = new String[]{"C:/Program Files/Git/bin/bash.exe",  scriptPath};

            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Building process ...");
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Redirect error stream to the input stream
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Starting process ...");
            Process process = pb.start();
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " " + ColorManager.colorText("Successfully", ColorManager.sucessColor) + " started process!");

            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Setting input stream reader ...");
            // Get input stream of the process (combined output and error stream)
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            boolean connected = false;

            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Now mirroring console of process!");
            // Read the output of the process
            String line;
            while (true) {
                // Read standard output and error lines if available
                while (bufferedReader.ready() && (line = bufferedReader.readLine()) != null) {
                    System.out.println(LogManager.executor() + LogManager.process() + LogManager.log() + " " + line);
                    Main.console.println(line);
                }

                if (!connected) {
                    try {
                        GenerationTracker.connect();
                        System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Connected to EvoAl!");
                        connected = true;
                    } catch (Exception ignored) {
                        // Handle exception as necessary
                    }
                }

                // Check if the process is terminated
                try {
                    int exitCode = process.exitValue();

                    System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Eaf closing port!");
                    try {
                        GenerationTracker.disconnect();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // Process has terminated, break out of the loop
                    if (exitCode == 0) {
                        System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Process " + ColorManager.colorText("finished successfully", ColorManager.sucessColor) + "!");
                        Main.console.print("Process finished ");
                        Main.console.printColored("successfully", ColorManager.sucessColor);
                        Main.console.println("!");
                    } else {
                        System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + LogManager.error() + " Process " + ColorManager.colorText("failed", ColorManager.errorColor) + " with error code: " + exitCode);
                        Main.console.print("Process ");
                        Main.console.printColored("failed", ColorManager.errorColor);
                        Main.console.println(" with error code: " + exitCode);
                    }

                    InputHandler.processTerminated();
                    break;
                } catch (IllegalThreadStateException e) {
                    // Process is still running, wait and continue reading
                    Thread.sleep(50); // 50 milliseconds pause
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void run() {
        ErrorPane.checkForErrors();
        if (ErrorPane.errors > 0) {
            SoundManager.playExclamationSound();
            Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(ErrorPane.first);
        } else {
            ScriptWriter.saveAndWriteEvoAlFiles();
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Preparing to execute ...");
            // Create and start a new thread to run execute()
            Thread executionThread = new Thread(() -> {
                try {
                    execute();
                } catch (Exception e) {
                    System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + LogManager.error() + " Script crashed: " + e);
                    InputHandler.processTerminated();
                }
            });
            executionThread.start();
        }
    }

}
