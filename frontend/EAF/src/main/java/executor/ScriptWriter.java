package executor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ScriptWriter {

    public enum ScriptType {
        SEARCH
    }

    public static String evoalVersion = "20240708-152016";
    public static String projectName = "genetic-programming";

    public static ScriptType projectType = ScriptType.SEARCH;

    public static void createScript(String path2, String build, ScriptType type) {
        String outputFilePath = path2;

        try {
            List<String> lines = new ArrayList<>();
            lines.add(getReplacementLines(build, type));

            Files.write(Paths.get(outputFilePath), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getReplacementLines(String build, ScriptType type) {
        return "#!/bin/sh\n" +
                "export DISPLAY=localhost:0.0\n" +
                "export EVOAL_HOME=$( cd -- \"$(dirname $0)/../../EvoAlBuilds/" + build + "/evoal\" >/dev/null 2>&1 ; pwd -P )\n" +
                "# Print the current working directory for debugging\n" +
                "echo \"Current working directory in second script: $(pwd)\"\n" +
                "echo \"EVOAL_HOME in second script: $EVOAL_HOME\"\n" +
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

    public static void main(String[] args) throws IOException, InterruptedException {
        String currentPath = System.getProperty("user.dir");


        createScript(currentPath + "\\EvoAlScripts\\" + projectName + "\\run.sh", evoalVersion, projectType);
        try {

            String scriptPath = currentPath + "\\EvoAlScripts\\genetic-programming\\run.sh";
            System.out.println("Current Path: " + currentPath);
            System.out.println("Script Path: " + currentPath + scriptPath);
            String[] command = new String[]{"C:\\Program Files\\Git\\bin\\bash.exe",  scriptPath};


            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Redirect error stream to the input stream

            Process process = pb.start();

            // Get input stream of the process (combined output and error stream)
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Read the output of the process
            String line;
            while (true) {
                // Read standard output and error lines if available
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }

                // Check if the process is terminated
                try {
                    int exitCode = process.exitValue();
                    // Process has terminated, break out of the loop
                    if (exitCode == 0) {
                        System.out.println("Process executed successfully");
                    } else {
                        System.out.println("Process failed with error code: " + exitCode);
                    }
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
}