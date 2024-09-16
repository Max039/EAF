package eaf.manager;

import eaf.download.Downloader;
import eaf.models.Pair;
import eaf.setup.Preset;
import org.json.JSONArray;
import org.json.JSONObject;
import eaf.input.InputHandler;
import eaf.Main;
import eaf.ui.panels.ErrorPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileManager {

    public static void loadSave(JSONObject o) {
        String preset = o.getString("preset");
        Main.preset = Preset.getPreset(preset);
        JSONArray a2 = o.getJSONArray("data");
        Main.dataPanel.fromJson(a2);
        JSONArray a = o.getJSONArray("rects");
        Main.mainPanel.leftPanel.fromJson(a);
        JSONArray a3 = o.getJSONArray("constants");
        Main.constantManager.fromJson(a3);
        InputHandler.actionHandler.reset();
        ErrorPane.checkForErrors();
    }

    public static void emptySave() {
        Main.preset = null;
        Main.dataPanel.fromJson(new JSONArray());
        Main.mainPanel.leftPanel.fromJson(new JSONArray());
        InputHandler.actionHandler.reset();
        ErrorPane.checkForErrors();
    }

    public static JSONObject createSave() {
        JSONObject o = new JSONObject();
        o.put("rects", Main.mainPanel.leftPanel.toJson());
        o.put("data", Main.dataPanel.toJson());
        o.put("constants", Main.constantManager.toJson());
        if (Main.preset != null) {
            o.put("preset", Main.preset.getName());
        }
        else {
            o.put("preset", "");
        }
        return o;
    }

    public static void writeJSONToFile(JSONObject jsonArray, String filePath) {
        write(jsonArray.toString(4), filePath);
    }

    public static void write(String content, String filePath) {
        try {
            LogManager.println(LogManager.fileManager() + LogManager.write() + " Saving to " + filePath);
            // Create a File object for the specified file path
            File file = new File(filePath);

            // Create the parent directories if they do not exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write the JSON data to the file
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(content);
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJSONFileToJSON(String filePath) throws IOException, org.json.JSONException {
        return new JSONObject(read(new FileReader(filePath)));
    }

    public static JSONObject readJSONFileToJSON(File file) throws IOException, org.json.JSONException {
        return new JSONObject(read(new FileReader(file)));
    }

    public static String read(String path) throws IOException {
        return read(new FileReader(path));
    }

    private static String read(FileReader fileReader) throws IOException, org.json.JSONException {
        // Open the file using a BufferedReader
        try (BufferedReader reader = new BufferedReader(fileReader)) {
            // Read the entire content of the file into a String
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Convert the String content to a JSONArray
            return jsonContent.toString();
        }
    }

    public static File chooseJavaFile(String path, String header, String ... filter) {
        String currentDirectory = System.getProperty("user.dir");

        // Specify the starting directory
        File startingDirectory = new File(currentDirectory + path);

        // Create a JFileChooser instance with the starting directory
        JFileChooser fileChooser = new JFileChooser(startingDirectory);

        // Set up the filter to only allow .json files
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter(header, filter);
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
                LogManager.println(LogManager.fileManager() + LogManager.file() + LogManager.warning() + " Recent file could not be opened!");
            }
        }
        else {
            LogManager.println(LogManager.fileManager() + LogManager.file() + LogManager.warning() + " No recent file in cache!");
        }
    }

    public static void newFile() {
        String saveName = JOptionPane.showInputDialog(null, "Enter the name for the save:", "Save Name", JOptionPane.QUESTION_MESSAGE);
        if (saveName == null || saveName.trim().isEmpty()) {
            // User canceled the input or entered an empty name
            LogManager.println(LogManager.fileManager() + LogManager.file() + " Save operation " + ColorManager.colorText("canceled", ColorManager.warningColor) + " or no name entered.");
            return;
        }
        // Construct the full path to the file
        String currentDirectory = System.getProperty("user.dir");
        File file = new File(currentDirectory + Main.savesPath + "/" + saveName, saveName + "." + Main.saveFormat);


        // Check if a file with the given name already exists
        if (file.exists()) {
            JOptionPane.showMessageDialog(null, "A file with that name already exists. Please choose a different name.", "Error", JOptionPane.ERROR_MESSAGE);
            newFile();
            return;
        }
        try {
            copyFolder(currentDirectory + "/project_base", currentDirectory  + Main.savesPath + "/" + saveName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        FileManager.emptySave();
        writeJSONToFile(createSave(), file.getAbsolutePath());
        Main.cacheManager.addToBuffer("filesOpened", file.getAbsolutePath());
        LogManager.println(LogManager.fileManager() + LogManager.file() + " File " + file.getName() + " " + ColorManager.colorText("saved", ColorManager.sucessColor) + "!");
    }

    public static void save() {
        var r = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        if (r != null) {
            writeJSONToFile(createSave(), r);
            InputHandler.actionHandler.saved();
        }
        else {
            FileManager.saveAs();
        }
        Main.mainPanel.revalidate();
        Main.mainPanel.repaint();
    }

    public static void saveAs() {
        String fileName = "save";
        var r = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        if (r != null) {
            var parts =  r.split("/");
            fileName = parts[parts.length - 1].split("\\.")[0];
        }

        var file = FileManager.saveJavaFile(Main.savesPath, Main.saveFormat, fileName);
        if (file != null) {
            FileManager.writeJSONToFile(FileManager.createSave(), file.getPath());
            Main.cacheManager.addToBuffer("filesOpened", file.getPath());
            InputHandler.actionHandler.saved();
            LogManager.println(LogManager.fileManager() + LogManager.file() + " File " + file.getName() + " " + ColorManager.colorText("saved", ColorManager.sucessColor) + "!");
        }
        Main.mainPanel.revalidate();
        Main.mainPanel.repaint();
    }


    public static void copyFolder(String source, String destination) throws IOException {
        Path sourceFolder = Path.of(source);
        Path destinationFolder = Path.of(destination);

        LogManager.println(LogManager.fileManager() + LogManager.write() + " Copying folder \"" + sourceFolder + "\" to \"" + destinationFolder + "\"");
        // Check if the source folder exists
        if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
            throw new IllegalArgumentException("Source folder doesn't exist or is not a directory.");
        }

        // Create the destination folder if it doesn't exist
        if (!Files.exists(destinationFolder)) {
            Files.createDirectories(destinationFolder);
        }

        // Iterate over the contents of the source folder
        Files.walk(sourceFolder).forEach(sourcePath -> {
            try {
                Path targetPath = destinationFolder.resolve(sourceFolder.relativize(sourcePath));

                if (Files.isDirectory(sourcePath)) {
                    // Create the directory in the target location
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                } else {
                    // Copy the file to the target location
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static File copyFile(String sourcePath, String targetPath) {
        // Create streams for reading and writing
        try (FileInputStream fis = new FileInputStream(sourcePath)) {

            // Extract the target directory from the target path
            File targetFile = new File(targetPath);
            File targetDirectory = targetFile.getParentFile();

            // Check if the directory exists; if not, create it
            if (targetDirectory != null && !targetDirectory.exists()) {
                if (targetDirectory.mkdirs()) {
                    LogManager.println(LogManager.fileManager() + LogManager.write() + " Target directory created successfully.");
                } else {
                    LogManager.println(LogManager.fileManager() + LogManager.write()  + LogManager.error()  + " Failed to create target directory.");
                    return null;  // Return null if the directory couldn't be created
                }
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                // Buffer to hold file contents
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read and write the file contents
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                LogManager.println(LogManager.fileManager() + LogManager.write() + " File copied successfully.");
                return targetFile;

            } catch (IOException e) {
                LogManager.println(LogManager.fileManager() + LogManager.write() + LogManager.error() + " Error occurred during file copy: " + e.getMessage());
                return null;  // Return null in case of error during file writing
            }

        } catch (IOException e) {
            LogManager.println(LogManager.fileManager() + LogManager.write() + LogManager.error() + " Error occurred during file copy: " + e.getMessage());
            return null;  // Return null in case of error during file reading
        }
    }


    public static void replaceContentOfFile(String path, ArrayList<Pair<String, String>> replacements) {
        LogManager.println(LogManager.fileManager() + LogManager.write() + " Replacing parts in File \"" + path + "\"");
        try {
            // Read the file content
            String content = new String(Files.readAllBytes(Paths.get(path)));

            // Perform the replacements
            for (Pair<String, String> pair : replacements) {
                LogManager.println(LogManager.fileManager() + LogManager.write() + " Replacing \"" + pair.getFirst() + "\" with \"" + pair.getSecond() + "\"");
                content = content.replace(pair.getFirst(), pair.getSecond());
            }

            // Write the modified content back to the file
            Files.write(Paths.get(path), content.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDirectoryEmpty(File directory) {
        // Check if the File object represents a directory
        if (directory.isDirectory()) {
            // Get the contents of the directory
            String[] contents = directory.list();
            // Return true if the directory is empty, false otherwise
            return contents == null || contents.length == 0;
        }
        // If the file is not a directory, return false
        return false;
    }

    public static File findFirstFileInReverseOrder(String folderPath) {
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                // Sort files by their names in reverse order
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f2.getName().compareTo(f1.getName());
                    }
                });

                // Return the first file in the sorted list
                return files[0];
            }
        }
        return null;
    }

    public static boolean folderExists(String directoryPath, String folderName) {
        Path dirPath = Paths.get(directoryPath);

        // Check if the provided path is a directory
        if (!Files.isDirectory(dirPath)) {
            LogManager.println("The provided path is not a directory.");
            return false;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry) && entry.getFileName().toString().equals(folderName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

        // Assume the first JAR file is the one we want to copy (this can be customized)
        File jarFile = jarFiles[0];

        // Determine the destination file path, using the new file name
        Path destination = Paths.get(destinationPath, newFileName);

        // Copy the JAR file to the destination path, replacing it if it already exists
        Files.copy(jarFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        LogManager.println(LogManager.fileManager() + LogManager.write() + " Copied " + jarFile.getName() + " to " + destinationPath + " as " + newFileName);
    }


    public static void copyFiles(Path sourceDir, Path targetDir) throws IOException {
        if (Files.exists(targetDir)) {
            LogManager.println("Target directory already exists. No files copied.");
            return;
        }

        Files.createDirectories(targetDir);

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = targetDir.resolve(sourceDir.relativize(file));
                Files.createDirectories(targetFile.getParent());
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Path targetDirPath = targetDir.resolve(sourceDir.relativize(dir));
                    Files.createDirectories(targetDirPath);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    public static void copyToDocuments() {
        URL location = FileManager.class.getProtectionDomain().getCodeSource().getLocation();


        try {
            // Convert URL to File
            File jarFile = new File(location.toURI());
            Path sourceDir = jarFile.getParentFile().toPath();
            Path targetDir = Paths.get(System.getProperty("user.home"), "Documents", "Eaf");
            copyFiles(sourceDir, targetDir);
            changeWorkingDirectory(targetDir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public static void changeWorkingDirectory(Path newDir) throws IOException {
        if (!Files.exists(newDir)) {
            throw new IOException("Directory does not exist: " + newDir);
        }
        System.setProperty("user.dir", newDir.toAbsolutePath().toString());
        Downloader.updatePaths();
        LogManager.println("Working directory changed to: " + System.getProperty("user.dir"));
    }
}
