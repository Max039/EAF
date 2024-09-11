package eaf.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileChangesChecker {

    public static final String JSON_FILE_NAME = "file_list.json";

    public static boolean updateFileJson(String folderPath, ArrayList<String> ignore) {
        try {
            // Get the current state of files in the directory and its subdirectories
            Map<String, Long> currentFileState = new HashMap<>();
            collectFiles(new File(folderPath), currentFileState, ignore);

            // Check if the JSON file exists
            File jsonFile = new File(folderPath + "/" + JSON_FILE_NAME);
            if (jsonFile.exists()) {

                // Read the existing JSON file
                String existingJsonContent = new String(Files.readAllBytes(Paths.get(folderPath + "/" + JSON_FILE_NAME)));
                JSONObject existingJson = new JSONObject(existingJsonContent);
                Map<String, Long> existingFileState = new HashMap<>();

                // Extract the existing file state from the JSON
                JSONArray filesArray = existingJson.getJSONArray("files");
                for (int i = 0; i < filesArray.length(); i++) {
                    JSONObject fileObject = filesArray.getJSONObject(i);
                    existingFileState.put(fileObject.getString("path"), fileObject.getLong("lastModified"));
                }
                var test = existingFileState.size() == currentFileState.size();

                if (test) {
                    for (var existing : existingFileState.entrySet()) {
                        var v = currentFileState.get(existing.getKey());
                        if (v == null || !v.equals(existing.getValue()) ) {
                            test = false;
                            break;
                        }
                    }
                }

                // Check for differences
                if (!test) {
                    // Update JSON file with the new state
                    return writeJsonToFile(folderPath, currentFileState);
                } else {
                    // No changes detected
                    return false;
                }
            } else {
                // No JSON file found, create a new one
                return writeJsonToFile(folderPath, currentFileState);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void collectFiles(File folder, Map<String, Long> fileState, ArrayList<String> ignore) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().equals(JSON_FILE_NAME)) {
                    var check = false;
                    for (var f : ignore) {
                        if (file.getPath().replace("\\" , "/").contains(f.replace("\\", "/"))) {
                            check = true;
                        }
                    }
                    if (!check) {
                        fileState.put(file.getAbsolutePath(), file.lastModified());
                    }

                } else if (file.isDirectory()) {
                    // Recursively collect files from subdirectories
                    collectFiles(file, fileState, ignore);
                }
            }
        }
    }

    private static boolean writeJsonToFile(String folderPath, Map<String, Long> fileState) {
        try {
            JSONArray filesArray = new JSONArray();
            for (Map.Entry<String, Long> entry : fileState.entrySet()) {
                JSONObject fileObject = new JSONObject();
                fileObject.put("path", entry.getKey());
                fileObject.put("lastModified", entry.getValue());
                filesArray.put(fileObject);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("files", filesArray);

            try (FileWriter fileWriter = new FileWriter(folderPath + "/" + JSON_FILE_NAME)) {
                fileWriter.write(jsonObject.toString(4)); // Indent with 4 spaces
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
