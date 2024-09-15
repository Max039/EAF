package eaf.executor;

import eaf.Main;
import eaf.input.InputHandler;
import eaf.manager.CacheManager;
import eaf.manager.ColorManager;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.sound.SoundManager;
import eaf.ui.UiUtil;
import eaf.ui.panels.ErrorPane;

import javax.swing.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Executor {

    public static boolean pwdSet = false;

    public static String ramVersionOfSudo = null;
    public static String ramTempVersionOfSudo = null;

    public static boolean makeExecutable(String filePath) {
        System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Making script executable ...");
        // Construct the chmod command
        String command = "chmod +x " + filePath;

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Wait for the command to complete
            int exitCode = process.waitFor();

            // Return true if the command was successful
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            // Print the exception if an error occurs
            e.printStackTrace();
            return false;
        }
    }

    public static void execute() throws IOException, InterruptedException {

        MavenProjectHandler.copyPlugins();

        ClassLocator.refreshEvoAlModules();

        String currentPath = System.getProperty("user.dir");
        var scriptPath = ScriptWriter.getPathToProject() + "/" + Main.preset.shName() + ".sh";
        Main.console.println("Script: " + scriptPath);
        System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Requesting script at " + scriptPath + " .... ");
        ScriptWriter.createScript(scriptPath, Main.evoalVersion);
        System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Trying to run script ...");
        try {

            System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Current path: " + currentPath);
            System.out.println(LogManager.executor() + LogManager.script() + LogManager.shell() + LogManager.status() + " Script path: " + scriptPath);



            String[] command;
            String sudoPwd = ramVersionOfSudo;

            switch (Main.os) {
                case MAC -> {
                    makeExecutable(scriptPath);
                    command = new String[]{"sudo","-S","/bin/bash", scriptPath};
                    if (sudoPwd == null) {
                        System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Please enter sudo pwd!");
                        pwdSet = false;
                        ramTempVersionOfSudo = null;
                        if (Main.nogui) {
                            Console console = System.console();
                            Executor.ramTempVersionOfSudo = new String(console.readPassword());
                            Executor.pwdSet = true;
                        }
                        else {
                            UiUtil.getPasswordFromUser();
                        }

                        while (!pwdSet) {
                            Thread.sleep(50);
                        }
                        sudoPwd = ramTempVersionOfSudo;

                        if (sudoPwd == null) {
                            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Password input cancelled.");
                            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Terminating startup ...");
                            InputHandler.processTerminated();
                            return;
                        }
                    }
                }
                case WINDOWS -> {
                    command = new String[]{"C:/Program Files/Git/bin/bash.exe",  scriptPath};
                    sudoPwd = "";
                }
                default -> {
                    InputHandler.processTerminated();
                    throw new RuntimeException("Cannot execute script no valid OS found!");
                }
            }


            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Building process ...");
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Redirect error stream to the input stream
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Starting process ...");
            Process process = pb.start();
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " " + ColorManager.colorText("Successfully", ColorManager.sucessColor) + " started process!");

            if(Main.os == Main.OS.MAC){
                try (OutputStream ops = process.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops))) {
                    writer.write(sudoPwd + "\n");
                    writer.flush();
                }
            }


            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Setting input stream reader ...");
            // Get input stream of the process (combined output and error stream)
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            boolean postStartCompleted = false;

            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Now mirroring console of process!");
            // Read the output of the process
            String line;
            while (true) {
                // Read standard output and error lines if available
                while (bufferedReader.ready() && (line = bufferedReader.readLine()) != null) {
                    System.out.println(LogManager.executor() + LogManager.process() + LogManager.log() + " " + line);
                    Main.console.println(line);
                }

                if (!postStartCompleted) {
                    postStartCompleted = Main.preset.postStart();
                }

                // Check if the process is terminated
                try {
                    int exitCode = process.exitValue();

                    Main.preset.postStop();

                    // Process has terminated, break out of the loop
                    if (exitCode == 0) {
                        ramVersionOfSudo = sudoPwd;
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
        if (Main.preset != null) {
            ErrorPane.checkForErrors();
            if (Main.mainPanel.leftPanel.getRects().isEmpty()) {
                SoundManager.playExclamationSound();
            }
            else {
                if (ErrorPane.errors > 0) {
                    SoundManager.playExclamationSound();
                    Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(ErrorPane.first);
                } else {
                    System.out.println(LogManager.scriptWriter() + LogManager.write() + " Saving project ...");
                    FileManager.save();
                    File currentProject = new File(Main.cacheManager.getFirstElement(String.class, "filesOpened"));
                    if (!currentProject.getAbsolutePath().endsWith("generator.eaf")) {
                        File generator = new File(currentProject.getParentFile().getAbsolutePath() + "/generator." + Main.saveFormat);
                        if (generator.exists()) {
                            try {
                                FileManager.loadSave(FileManager.readJSONFileToJSON(generator.getAbsolutePath()));
                                Main.cacheManager.addToBuffer("filesOpened", generator.getAbsolutePath());
                                run();
                                while (Main.processRunning) {
                                    Thread.sleep(50);
                                }

                                FileManager.loadSave(FileManager.readJSONFileToJSON(currentProject.getAbsolutePath()));
                                Main.cacheManager.addToBuffer("filesOpened", currentProject.getAbsolutePath());

                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    ScriptWriter.writeEvoAlFiles();
                    System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Preparing to execute ...");
                    // Create and start a new thread to run execute()
                    InputHandler.processStarted();
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
        else {
            JOptionPane.showMessageDialog(Main.mainFrame, "Error you need to select a preset first!.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

}
