package eaf.download;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eaf.Main;
import eaf.input.InputHandler;
import eaf.manager.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import eaf.models.Pair;

import javax.swing.*;


public class Downloader {

    private static final String GITLAB_URL = "https://gitlab.informatik.uni-bremen.de/api/v4";
    private static final String PROJECT_ID = "evoal%2Fsource%2Fevoal-core"; // URL-encoded project ID
    private static final String PRIVATE_TOKEN = "oWVzZS_JquJwxFhqnx4y"; // Read api only - never expires

    public static String PATH = System.getProperty("user.dir") + "/" + Main.evoalBuildFolder;

    public static String DOWNLOAD_PATH = PATH + "/";

    private static final String defaultBranch = "develop";

    private static final String artifactName = "all:package";

    private static int numberOfVersionsToShow = 100;

    private static JComboBox<String> versionComboBox = new JComboBox<>();
    private JButton mainButton;

    private static JPanel panel = new JPanel();

    private static String outdated = "outdated / unsupported";

    private static String local = "local";

    private static String downloaded = "downloaded";

    static JSONArray defaultBranchPipelines = null;

    static JSONArray allPipelines = null;

    static ArrayList<String> allPipelinesStings = null;

    static ArrayList<Pair<String, String>> allPipelinesApprovedStings = null;

    static String lastAllResponse = "";

    public static int progressBarWidth = 40;

    public static JFrame frame = null;

    static {
        versionComboBox.setRenderer(new MyHtmlComboBoxRenderer());
    }

    public static void showMenu() {
        frame = new JFrame();

        frame.setTitle("Artifact Downloader");
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        panel.setLayout(new BorderLayout());

        // Create the dropdown menu (JComboBox)
        String[] options = {
                "Download selected version",
                "Delete outdated versions",
                "Open in file explorer",
                "Delete selected installation",
                "Change to selected version"
        };
        JComboBox<String> optionsDropdown = new JComboBox<>(options);

        // Create the OK button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread executionThread = new Thread(() -> {
                    String selectedOption = (String) optionsDropdown.getSelectedItem();
                    switch (selectedOption) {
                        case "Download selected version":
                            String selectedVersion = (String) versionComboBox.getSelectedItem();
                            if (selectedVersion != null && !selectedVersion.contains("local") && !selectedVersion.contains("outdated")) {
                                selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                                try {
                                    downloadSelectedVersion(selectedVersion, false, true);
                                    JOptionPane.showMessageDialog(frame,
                                            "Successfully downloaded and activated EvoAl Build " + selectedVersion, "Info", JOptionPane.INFORMATION_MESSAGE);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(frame,
                                            "Failed to download artifact: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                InputHandler.setEvoAlVersion(selectedVersion);
                                populateVersions();
                            }
                            break;

                        case "Delete outdated versions":
                            try {
                                deleteNonMatchingFolders(new File(PATH), allPipelinesApprovedStings);
                                JOptionPane.showMessageDialog(frame,
                                        "All outdated versions deleted!", "Info", JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            populateVersions();
                            break;

                        case "Open in file explorer":
                            selectedVersion = (String) versionComboBox.getSelectedItem();
                            selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                            if (Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
                                openExplorer(DOWNLOAD_PATH + selectedVersion);
                            }
                            else {
                                JOptionPane.showMessageDialog(frame,
                                        "Cannot open file explorer version not present on hard drive!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            populateVersions();
                            break;

                        case "Delete selected installation":
                            selectedVersion = (String) versionComboBox.getSelectedItem();
                            selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                            if (Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
                                try {
                                    deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                                populateVersions();
                            }
                            break;
                        case "Change to selected version":
                            selectedVersion = (String) versionComboBox.getSelectedItem();
                            selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                            if (Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
                                InputHandler.setEvoAlVersion(selectedVersion);
                                JOptionPane.showMessageDialog(frame,
                                        "Successfully change to EvoAl Build " + selectedVersion, "Info", JOptionPane.INFORMATION_MESSAGE);
                            }
                            break;

                        default:
                            break;
                    }
                });
                executionThread.start();
            }
        });

        // Panel for the dropdown and OK button
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(optionsDropdown);
        controlPanel.add(okButton);

        panel.add(controlPanel, BorderLayout.SOUTH);

        // Most recent button
        JButton mostRecentButton = new JButton("Download newest version");
        mostRecentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadNewestVersionIfNeeded(true, true);
                populateVersions();
            }
        });
        panel.add(mostRecentButton, BorderLayout.NORTH);

        // Refresh button
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    refreshPipelinesOnNextRequest();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                populateVersions();
            }
        });
        panel.add(refresh, BorderLayout.EAST);
        panel.add(versionComboBox, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void update() {
        populateVersions();
        Main.setIndex(true);
    }

    public static void openExplorer(String path) {
        File file = new File(path);
        if (!file.exists()) {
            LogManager.println(LogManager.downloader() + " Directory or file does not exist.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                LogManager.println(LogManager.downloader() + " Desktop is not supported.");
            }
        } catch (IOException e) {
            LogManager.println(LogManager.downloader() + " Error opening file explorer: " + e.getMessage());
        }
    }

    public static void checkForUpdate() {
        try {
            JSONArray pipelines = getSuccessfulPipelines(true);
            LogManager.println(LogManager.downloader() + " Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow );
            for (int i = 0; i < pipelines.length(); i++) {
                JSONObject pipeline = pipelines.getJSONObject(i);
                String updatedAt = pipeline.getString("updated_at");
                String versionName = getVersionNameFromDate(updatedAt);
                if (hasJob(versionName, true)) {
                    if (!Files.exists(Paths.get(DOWNLOAD_PATH + versionName))) {

                        // Create the frame
                        JFrame frame = new JFrame("EvoAl Updater");

                        // Create the message with the version number
                        String message = "There is a new build of EvoAl " + versionName + " would you like to download and activate?";

                        // Create a checkbox for "Uninstall old outdated"
                        JCheckBox uninstallCheckBox = new JCheckBox("Uninstall outdated");

                        uninstallCheckBox.setSelected(true);

                        // Object array to hold the message and the checkbox
                        Object[] params = {message, uninstallCheckBox};

                        // Create an option pane with Yes and No options
                        int option = JOptionPane.showConfirmDialog(frame, params, "Update Available", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        // Check the user's choice
                        if (option == JOptionPane.YES_OPTION) {
                            Thread executionThread = new Thread(() -> {
                                LogManager.println(LogManager.downloader() + " New EvoAl version found!");
                                downloadIfNeeded(versionName, true, true);
                                SwingUtilities.invokeLater(() -> {
                                    InputHandler.setEvoAlVersion(versionName);
                                });

                                if (uninstallCheckBox.isSelected()) {
                                    populateVersions();
                                    try {
                                        deleteNonMatchingFolders(new File(PATH), allPipelinesApprovedStings);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    populateVersions();
                                }
                                Main.setUpdateChecked(true);
                            });
                            executionThread.start();
                        } else if (option == JOptionPane.NO_OPTION) {
                            Main.setUpdateChecked(true);
                        } else {
                            // This block will handle the case when the window is closed without choosing Yes or No
                            Main.setUpdateChecked(true);
                        }

                        // Close the frame after the user makes a choice or closes the window
                        frame.dispose();
                        return;
                    }
                    else {
                        LogManager.println(LogManager.downloader() + " Newest EvoAl version already downloaded!");
                        return;
                    }
                }
            }


        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    //Gets the newest nersion
    public static void downloadNewestVersionIfNeeded(boolean downloadWindow, boolean limitBranches) {
        try {
            JSONArray pipelines = getSuccessfulPipelines(limitBranches);
            LogManager.println(LogManager.downloader() + " Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow );
            for (int i = 0; i < pipelines.length(); i++) {
                JSONObject pipeline = pipelines.getJSONObject(i);
                String updatedAt = pipeline.getString("updated_at");
                String versionName = getVersionNameFromDate(updatedAt);
                try {
                    downloadIfNeeded(versionName, downloadWindow, limitBranches);
                    return;
                }
                catch (Exception e) {
                    if (e instanceof JobNotFoundException || e instanceof  DownloadFailedException) {
                        LogManager.println(LogManager.downloader() + " Skipping version " + versionName);
                        if (i == pipelines.length() - 1 && !limitBranches) {
                            throw new RuntimeException("No valid version was found!");
                        }
                    }
                    else {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (limitBranches) {
                downloadNewestVersionIfNeeded(downloadWindow, false);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void downloadIfNeeded(String versionName, boolean downloadWindow, boolean limitBranches) {
        if (!Files.exists(Paths.get(DOWNLOAD_PATH + versionName))) {
            try {
                downloadSelectedVersion(versionName, limitBranches, downloadWindow);
            } catch (Exception ex) {
                if (ex instanceof JobNotFoundException) {
                    throw new JobNotFoundException(ex.getMessage());
                }
                else if (ex instanceof DownloadFailedException) {
                    throw new DownloadFailedException(ex.getMessage());
                }
                else {
                    throw new RuntimeException(ex);
                }

            }
        }
        else {
            LogManager.println(LogManager.downloader() + " " + DOWNLOAD_PATH + versionName);
            LogManager.println(LogManager.downloader() + " Version " + versionName + " already present on filesystem!");
            JOptionPane.showMessageDialog(frame,
                    "Version " + versionName + " already present on filesystem!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    // Custom renderer class for HTML content
    static class MyHtmlComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                // Use the default rendering

                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setText(value.toString()); // Set the text to HTML formatted string
                }

            }
            return c;
        }
    }

    public static void refreshPipelinesOnNextRequest() throws IOException {
        String lastAllResponseCopy = lastAllResponse;
        getSuccessfulPipelines(false);
        if (!lastAllResponseCopy.equals(lastAllResponse)) {
            allPipelines = null;
            defaultBranchPipelines = null;
            allPipelinesStings = null;
            allPipelinesApprovedStings = null;
        }


    }

    public static void populateVersions() {
        versionComboBox.removeAllItems();
        try {
            getPipelinesWithValidPackage();

            for (var result : allPipelinesApprovedStings) {
                String build = result.getFirst() + result.getSecond();
                if (Files.exists(Paths.get(DOWNLOAD_PATH + result.getFirst()))) {
                    build += " (<font color='green'>" + downloaded + "</font>)";
                }

                String finalBuild = build;
                versionComboBox.addItem("<html>" + finalBuild + "</html>");
            }

            versionComboBox.revalidate();

            ArrayList<String> approvedStrings = (ArrayList<String>) allPipelinesApprovedStings.stream()
                    .map(t -> t.getFirst())
                    .collect(Collectors.toList());


            addUniqueFoldersToComboBox(approvedStrings, allPipelinesStings);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to fetch versions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void getPipelinesWithValidPackage() throws Exception {
        if (allPipelinesStings == null || allPipelinesApprovedStings == null) {

            allPipelinesStings = new ArrayList<>();
            allPipelinesApprovedStings = new ArrayList<>();
            try {
                JSONArray pipelines = getSuccessfulPipelines(false);
                LogManager.println(LogManager.downloader() + " Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow);
                LogManager.println(LogManager.downloader() + " Indexing pipelines that have valid artifact ...");
                CountDownLatch latch = new CountDownLatch(pipelines.length());
                AtomicInteger count = new AtomicInteger();

                // Initialize the progress bar variables
                long totalTasks = pipelines.length();

                for (int i = 0; i < pipelines.length(); i++) {
                    int index = i;
                    CompletableFuture.supplyAsync(() -> {
                        JSONObject pipeline = pipelines.getJSONObject(index);
                        String updatedAt = pipeline.getString("updated_at");
                        String versionName = getVersionNameFromDate(updatedAt);
                        allPipelinesStings.add(versionName);
                        String webUrl = pipeline.getString("web_url");
                        Pair<Boolean, String> hasAllPackageJob;
                        try {
                            hasAllPackageJob = hasAllPackageJob(webUrl);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if (hasAllPackageJob.getFirst()) {
                            String s = versionName;
                            return new Pair<>(s, " [Branch=\"" + hasAllPackageJob.getSecond() + "\"]");
                        }
                        return null;
                    }).thenAccept(result -> {
                        if (result != null) {
                            allPipelinesApprovedStings.add(result);
                            count.getAndIncrement();
                        }
                        latch.countDown();
                        // Update the progress bar
                        updateProgressBar(totalTasks - latch.getCount(), totalTasks, progressBarWidth);
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        latch.countDown();
                        updateProgressBar(totalTasks - latch.getCount(), totalTasks, progressBarWidth);
                        return null;
                    });
                }

                latch.await();  // Wait for all tasks to complete
                LogManager.println();
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }


    private static void addUniqueFoldersToComboBox(ArrayList items, ArrayList all) {
        try {
            Path downloadPath = Paths.get(DOWNLOAD_PATH);
            if (Files.exists(downloadPath) && Files.isDirectory(downloadPath)) {
                try (Stream<Path> paths = Files.walk(downloadPath, 1)) {
                    ArrayList<String> folderNames = (ArrayList<String>) paths.filter(Files::isDirectory)
                            .map(path -> path.getFileName().toString())
                            .collect(Collectors.toList());

                    // Get existing items in the versionComboBox
                    Set<String> existingItems = new HashSet<>();
                    for (int i = 0; i < items.size(); i++) {
                        existingItems.add((String) items.get(i));
                    }

                    boolean skipRoot = true;
                    for (String folderName : folderNames) {
                        if (!skipRoot) {
                            boolean isContained = false;
                            // Check if folder name is already contained in any of the existing items
                            for (String item : existingItems) {
                                if (item.contains(folderName)) {
                                    isContained = true;
                                    break;
                                }
                            }
                            if (!isContained) {
                                isContained = false;
                                // Check if folder name is already contained in any of the existing items
                                for (var item : all) {
                                    String t = (String) item;
                                    if (t.contains(folderName)) {
                                        isContained = true;
                                        break;
                                    }
                                }
                                if (isContained) {
                                    SwingUtilities.invokeLater(() -> versionComboBox.addItem("<html>" + folderName + " (<font color='red'>" + outdated + "</font>)</html> "));
                                }
                                else {
                                    SwingUtilities.invokeLater(() -> versionComboBox.addItem("<html>" + folderName + " (<font color='orange'>" + local + "</font>)</html> "));

                                }
                            }
                        }
                        else {
                            skipRoot = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Pair<Boolean, String> hasAllPackageJob(String webUrl) throws IOException {
        String buildUrl = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines" + webUrl.split("pipelines")[1] + "/jobs";
        //LogManager.println("Checking pipeline if artifact " + artifactName + " exists : " + buildUrl);

        HttpURLConnection connection = (HttpURLConnection) new URL(buildUrl).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        try {
            if (connection.getResponseCode() == 200) {
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();

                    // Parse JSON response
                    JSONArray jobsArray = new JSONArray(jsonResponse);

                    // Check each job in the array
                    for (int i = 0; i < jobsArray.length(); i++) {
                        JSONObject job = jobsArray.getJSONObject(i);
                        String jobName = job.getString("name");
                        if (jobName.contains(artifactName)) {
                            JSONArray artifacts = job.getJSONArray("artifacts");
                            for (int j = 0; j < artifacts.length(); j++) {
                                // Check if the job contains the artifactName
                                JSONObject art = artifacts.getJSONObject(j);
                                if (art.getString("file_type").equals("archive")) {
                                    return new Pair<>(true, job.getString("ref")); // If any job doesn't have the artifactName, return false
                                }
                            }
                            return new Pair<>(false, "");
                        }
                    }
                }
            } else {
                throw new IOException("Failed to get builds: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            throw e; // Rethrow the exception to propagate it
        } finally {
            connection.disconnect();
        }
        return new Pair<>(false, "");
    }

    private static JSONArray getSuccessfulPipelines(boolean limitBranch) throws IOException {
        fetchPipeline(limitBranch);
        if (limitBranch) {
            return defaultBranchPipelines;
        }
        else {
            return allPipelines;
        }
    }

    private static void fetchPipeline(boolean limitBranch) throws IOException {
        if ((limitBranch && defaultBranchPipelines != null) || (!limitBranch && allPipelines != null)) {
            return;
        }
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines?status=success";
        if (limitBranch) {
            url += "&ref=" + defaultBranch;
        }
        url += "&order_by=id&sort=desc&per_page=" + numberOfVersionsToShow;
        LogManager.println(LogManager.downloader() + " Requesting successful pipelines from branch " + defaultBranch + " requiring artifact " + artifactName + " : " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                if (limitBranch) {
                    defaultBranchPipelines = new JSONArray(response);
                }
                else {
                    allPipelines = new JSONArray(response);
                    lastAllResponse = response;
                }
            }
        } else {
            throw new IOException("Failed to get pipelines: " + connection.getResponseMessage());
        }
    }




    public static void deleteDirectory(File fileOrDirectory) throws Exception {
        if (fileOrDirectory.isDirectory()) {
            // List all files in the directory
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Recursively delete each file
                    deleteDirectory(file);
                }
            }
        }

        // Delete the directory itself
        if (!fileOrDirectory.delete()) {
            throw new Exception("Failed to delete " + fileOrDirectory);
        }
    }

    public static void deleteNonMatchingFolders(File directory, ArrayList<Pair<String, String>> retainFolderNames) throws Exception {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter must be a directory.");
        }

        // List all files (folders) in the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Check if the folder name matches the retainFolderNamed
                    if (allPipelinesStings.stream().anyMatch(t -> t.contains(file.getName())) && !retainFolderNames.stream().map(t -> t.getFirst()).collect(Collectors.toList()).stream().anyMatch(t -> t.equals(file.getName()))) {
                        // Recursively delete this folder and its contents
                        deleteDirectory(file);
                    }
                }
            }
        }
    }

    private static void downloadSelectedVersion(String selectedVersion, boolean limitBranch, boolean downloadWindow) throws Exception {
        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            _downloadSelectedVersion(selectedVersion, limitBranch, downloadWindow);
        } else {
            int choice = JOptionPane.showConfirmDialog(Main.mainFrame,
                    "The selected version is already downloaded. Do you want to re-download it now?",
                    "Download Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                LogManager.println(LogManager.downloader() + " Deleting Artifact " + selectedVersion);
                deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                _downloadSelectedVersion(selectedVersion, limitBranch, downloadWindow);
            }
        }

    }


    private static boolean hasJob(String selectedVersion, boolean limitBranch) throws Exception {
        JSONObject pipeline = findPipelineByVersion(selectedVersion, limitBranch);
        int pipelineId = pipeline.getInt("id");
        int jobId = getJobId(pipelineId, artifactName);
        return jobId != -1;
    }


    private static void _downloadSelectedVersion(String selectedVersion, boolean limitBranch, boolean downloadWindow) throws Exception {
        String outputFilePath = DOWNLOAD_PATH + selectedVersion + "/all-package-archive.zip";
        String extractToPath = DOWNLOAD_PATH + selectedVersion + "/";

        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            Files.createDirectories(Paths.get(DOWNLOAD_PATH + selectedVersion));
            JSONObject pipeline = findPipelineByVersion(selectedVersion, limitBranch);
            int pipelineId = pipeline.getInt("id");
            int jobId = getJobId(pipelineId, artifactName);

            if (jobId != -1) {
                try {
                    LogManager.println(LogManager.downloader() + " Downloading Artifact " + selectedVersion + " ... ");
                    try {
                        downloadArtifact(jobId, outputFilePath, downloadWindow);
                    }
                    catch (Exception e) {
                        throw new DownloadFailedException(e.getMessage());
                    }

                    extractZip(outputFilePath, extractToPath);
                    Files.deleteIfExists(Paths.get(outputFilePath));
                    LogManager.println(LogManager.downloader() + " Download complete");
                    //populateVersions();
                    //JOptionPane.showMessageDialog(Main.mainFrame, "Downloaded and extracted: " + selectedVersion,
                    //       "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Exception e) {
                    deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));

                    if (e instanceof DownloadFailedException) {
                        throw new DownloadFailedException(e.getMessage());
                    }
                    else {
                        throw new Exception(e);
                    }
                }
            } else {
                deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                LogManager.println(LogManager.downloader() + " Job with artifact '" + artifactName + "' not found for version " + selectedVersion + " !");
                throw new JobNotFoundException("Job with artifact '" + artifactName + "' not found for version " + selectedVersion + " !");
                //JOptionPane.showMessageDialog(Main.mainFrame, "Job with artifact '" + artifactName + "' not found.",
                        //"Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(Main.mainFrame, "Artifact " + selectedVersion + " already downloaded!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static class DownloadFailedException extends RuntimeException {
        public DownloadFailedException(String s) {
            super(s);
        }
    }

    public static class JobNotFoundException extends RuntimeException {
        public JobNotFoundException(String s) {
            super(s);
        }
    }

    private static JSONObject findPipelineByVersion(String versionName, boolean limitBranch) throws IOException {
        JSONArray pipelines = getSuccessfulPipelines(limitBranch);
        for (int i = 0; i < pipelines.length(); i++) {
            JSONObject pipeline = pipelines.getJSONObject(i);
            String updatedAt = pipeline.getString("updated_at");
            String currentVersion = getVersionNameFromDate(updatedAt);
            if (currentVersion.equals(versionName)) {
                return pipeline;
            }
        }
        throw new IOException("Pipeline for version " + versionName + " not found.");
    }

    private static int getJobId(int pipelineId, String jobName) throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines/" + pipelineId + "/jobs";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                JSONArray jobs = new JSONArray(response);
                for (int i = 0; i < jobs.length(); i++) {
                    JSONObject job = jobs.getJSONObject(i);
                    if (job.getString("name").equals(jobName)) {
                        return job.getInt("id");
                    }
                }
            }
        } else {
            throw new IOException("Failed to get jobs: " + connection.getResponseMessage());
        }
        return -1;
    }

    private static void downloadArtifact(int jobId, String outputFileName, boolean downloadWindow) throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/jobs/" + jobId + "/artifacts";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(outputFileName)) {

                long fileSize = connection.getContentLengthLong();
                byte[] buffer = new byte[1024];
                int count;
                long downloadedSize = 0;

                LogManager.print(LogManager.downloader() + " Downloading... [");


                final DownloadProgressWindow[] window = new DownloadProgressWindow[1];
                if (downloadWindow) {
                    Thread executionThread = new Thread(() -> {
                        window[0] = new DownloadProgressWindow();
                    });
                    executionThread.start();
                    while (executionThread.isAlive()) {
                        Thread.sleep(50);
                    }
                }

                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                    downloadedSize += count;
                    updateProgressBar(downloadedSize, fileSize, progressBarWidth);
                    if (downloadWindow) {
                        window[0].setProgress((int) ((double)downloadedSize/(double)fileSize * 100.0));
                    }
                }

                if (downloadWindow) {
                    window[0].stop();
                }
                LogManager.println("\n"+ LogManager.downloader() + " Artifact downloaded successfully to " + outputFileName);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IOException("Failed to download artifacts: " + connection.getResponseMessage());
        }
    }

    private static void updateProgressBar(long downloadedSize, long fileSize, int progressBarWidth) {
        double progress = (double) downloadedSize / fileSize * 100;
        int progressChars = (int) (progress / (100.0 / progressBarWidth));
        String progressBar = "=".repeat(progressChars);
        String emptyProgressBar = " ".repeat(progressBarWidth - progressChars);
        LogManager.printf("\r" + "[%s%s] %.2f%%", progressBar, emptyProgressBar, progress);
    }

    private static void extractZip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(dir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        LogManager.println(LogManager.downloader() + " Artifact extracted successfully to " + destDir);
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static String getVersionNameFromDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown-version";
        }
    }

    public static void updatePaths() {
        Downloader.PATH = System.getProperty("user.dir") + "/" + Main.evoalBuildFolder;
        Downloader.DOWNLOAD_PATH = Downloader.PATH + "/";
    }

}