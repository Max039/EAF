package test;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class FileManager {
    public static void writeToFile(String content, String filePath) {
        System.out.println("Writing to " + filePath);

        FileWriter writer = null;
        try {
            // Create a File object for the specified file path
            File file = new File(filePath);

            // Create the parent directories if they do not exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Initialize the FileWriter with the file object
            writer = new FileWriter(file);
            writer.write(content);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("An error occurred while closing the writer.");
                e.printStackTrace();
            }
        }
    }

    public static void loadSave(JSONObject o) {
        JSONArray a2 = o.getJSONArray("data");
        Main.dataPanel.fromJson(a2);
        JSONArray a = o.getJSONArray("rects");
        Main.mainPanel.leftPanel.fromJson(a);
        InputHandler.actionHandler.reset();
    }

    public static JSONObject createSave() {
        JSONObject o = new JSONObject();
        o.put("rects", Main.mainPanel.leftPanel.toJson());
        o.put("data", Main.dataPanel.toJson());
        return o;
    }

    public static void writeJSONToFile(JSONObject jsonArray, String filePath) {
        try {
            System.out.println("Saving to " + filePath);
            // Create a File object for the specified file path
            File file = new File(filePath);

            // Create the parent directories if they do not exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write the JSON data to the file
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonArray.toString(4));
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJSONFileToJSON(String filePath) throws IOException, org.json.JSONException {
        // Open the file using a BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read the entire content of the file into a String
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Convert the String content to a JSONArray
            return new JSONObject(jsonContent.toString());
        }
    }

    public static JSONObject readJSONFileToJSON(File file) throws IOException, org.json.JSONException {
        // Open the file using a BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read the entire content of the file into a String
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Convert the String content to a JSONArray
            return new JSONObject(jsonContent.toString());
        }
    }

    public static File chooseJavaFile(String path, String filter) {
        String currentDirectory = System.getProperty("user.dir");

        // Specify the starting directory
        File startingDirectory = new File(currentDirectory + path);

        // Create a JFileChooser instance with the starting directory
        JFileChooser fileChooser = new JFileChooser(startingDirectory);

        // Set up the filter to only allow .json files
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter(filter + " Files", filter);
        fileChooser.setFileFilter(jsonFilter);

        // Optionally set it to only show files with the given extension and hide others
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show the file chooser dialog
        int returnValue = fileChooser.showOpenDialog(null);

        // Check if the user selected a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public static File saveJavaFile(String path, String filter, String defaultFileName) {
        String currentDirectory = System.getProperty("user.dir");

        // Specify the starting directory
        File startingDirectory = new File(currentDirectory + path);

        // Create a JFileChooser instance with the starting directory
        JFileChooser fileChooser = new JFileChooser(startingDirectory);

        // Set up the filter to only allow specific file types
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(filter + " Files", filter);
        fileChooser.setFileFilter(fileFilter);

        // Set default file name
        fileChooser.setSelectedFile(new File(defaultFileName + "." + filter));

        // Optionally set it to only show files with the given extension and hide others
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show the save file dialog
        int returnValue = fileChooser.showSaveDialog(null);

        // Check if the user selected a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Ensure the file has the correct extension
            if (!selectedFile.getName().endsWith("." + filter)) {
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + filter);
            }

            return selectedFile;
        } else {
            return null;
        }
    }

    public static void loadRecent() {
        var c = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        if (c != null) {
            try {
                loadSave(readJSONFileToJSON(c));
            }
            catch (Exception e) {
                System.out.println("Recent file could not be opened");
            }
        }
        else {
            System.out.println("No recent file in cache!");
        }
    }
}
