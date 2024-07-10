package download;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import test.Pair;

import javax.swing.*;


public class Downloader extends JFrame {

    private static final String GITLAB_URL = "https://gitlab.informatik.uni-bremen.de/api/v4";
    private static final String PROJECT_ID = "evoal%2Fsource%2Fevoal-core"; // URL-encoded project ID
    private static final String PRIVATE_TOKEN = "oMAm4zMJVy9xc35PxQZg"; // Replace with your personal access token

    private static final String PATH = "EvoAlBuilds";

    private static final String DOWNLOAD_PATH = PATH + "/";

    private static final String defaultBranch = "develop";

    private static final String artifactName = "all:package";

    private static int numberOfVersionsToShow = 100;

    private JComboBox<String> versionComboBox;
    private JButton mainButton;

    private JPanel panel;

    private String outdated = "outdated / unsupported";

    private String local = "local";

    private String downloaded = "downloaded";

    JSONArray defaultBranchPipelines = null;

    JSONArray allPipelines = null;

    ArrayList<String> allPipelinesStings = null;

    ArrayList<Pair<String, String>> allPipelinesApprovedStings = null;

    String lastAllResponse = "";

    public static int progressBarWidth = 40;

    public Downloader() {
        setTitle("Artifact Downloader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create the main button that will show the dropdown menu
        mainButton = new JButton("Options");
        JPopupMenu popupMenu = new JPopupMenu();

        // Download selected version menu item
        JMenuItem downloadSelectedVersionItem = new JMenuItem("Download selected version");
        downloadSelectedVersionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedVersion = (String) versionComboBox.getSelectedItem();
                if (selectedVersion != null && !selectedVersion.contains("local") && !selectedVersion.contains("outdated")) {
                    selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                    try {
                        downloadSelectedVersion(selectedVersion, false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(Downloader.this,
                                "Failed to download artifact: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    populateVersions();
                }
            }
        });
        popupMenu.add(downloadSelectedVersionItem);

        // Delete outdated versions menu item
        JMenuItem deleteOutdatedVersionsItem = new JMenuItem("Delete outdated versions");
        deleteOutdatedVersionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {

                    deleteNonMatchingFolders(new File(PATH), allPipelinesApprovedStings);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                populateVersions();
            }
        });
        popupMenu.add(deleteOutdatedVersionsItem);

        JMenuItem openFileExplorer = new JMenuItem("Open in file explorer");
        openFileExplorer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedVersion = (String) versionComboBox.getSelectedItem();
                selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                if (Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
                    openExplorer(DOWNLOAD_PATH + selectedVersion);
                }
                populateVersions();
            }
        });
        popupMenu.add(openFileExplorer);

        // Delete outdated versions menu item
        JMenuItem delete = new JMenuItem("Delete selected installation");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedVersion = (String) versionComboBox.getSelectedItem();
                selectedVersion = selectedVersion.split("<html>")[1].split(" ")[0];
                if (Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {

                    try {
                        deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    populateVersions();
                }

            }
        });
        popupMenu.add(delete);

        // Add action listener to the main button to show the popup menu
        mainButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(mainButton, mainButton.getWidth() / 2, mainButton.getHeight() / 2);
            }
        });
        panel.add(mainButton, BorderLayout.SOUTH);

        // Most recent button
        JButton mostRecentButton = new JButton("Download newest version");
        mostRecentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadNewestVersionIfNeeded();
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

        add(panel);
    }

    public static void openExplorer(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Directory or file does not exist.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Desktop is not supported.");
            }
        } catch (IOException e) {
            System.out.println("Error opening file explorer: " + e.getMessage());
        }
    }


    //Gets the newest nersion
    public void downloadNewestVersionIfNeeded() {
        try {
            JSONArray pipelines = getSuccessfulPipelines(true);
            System.out.println("Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow );
            JSONObject pipeline = pipelines.getJSONObject(0);
            String updatedAt = pipeline.getString("updated_at");
            String versionName = getVersionNameFromDate(updatedAt);
            downloadIfNeeded(versionName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void downloadIfNeeded(String versionName) {
        if (!Files.exists(Paths.get(DOWNLOAD_PATH + versionName))) {
            try {
                downloadSelectedVersion(versionName, true);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            System.out.println("Version " + versionName + " already present on filesystem!");
        }

    }

    // Custom renderer class for HTML content
    class MyHtmlComboBoxRenderer extends DefaultListCellRenderer {
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

    public void refreshPipelinesOnNextRequest() throws IOException {
        String lastAllResponseCopy = lastAllResponse;
        getSuccessfulPipelines(false);
        if (!lastAllResponseCopy.equals(lastAllResponse)) {
            allPipelines = null;
            defaultBranchPipelines = null;
            allPipelinesStings = null;
            allPipelinesApprovedStings = null;
        }


    }

    public void populateVersions() {
        if (versionComboBox != null) {
            panel.remove(versionComboBox);
        }

        versionComboBox = new JComboBox<>();
        versionComboBox.setRenderer(new MyHtmlComboBoxRenderer());
        panel.add(versionComboBox, BorderLayout.CENTER);

        try {
            getPipelinesWithValidPackage();

            for (var result : allPipelinesApprovedStings) {
                String build = result.getFirst() + result.getSecond();
                if (Files.exists(Paths.get(DOWNLOAD_PATH + result.getFirst()))) {
                    build += " (<font color='green'>" + downloaded + "</font>)";
                }

                String finalBuild = build;
                SwingUtilities.invokeLater(() -> versionComboBox.addItem("<html>" + finalBuild + "</html>"));
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

    public void getPipelinesWithValidPackage() throws Exception {
        if (allPipelinesStings == null || allPipelinesApprovedStings == null) {

            allPipelinesStings = new ArrayList<>();
            allPipelinesApprovedStings = new ArrayList<>();
            try {
                JSONArray pipelines = getSuccessfulPipelines(false);
                System.out.println("Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow);
                System.out.println("Indexing pipelines that have valid artifact ...");
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
                System.out.println();
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }


    private void addUniqueFoldersToComboBox(ArrayList items, ArrayList all) {
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


    private Pair<Boolean, String> hasAllPackageJob(String webUrl) throws IOException {
        String buildUrl = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines" + webUrl.split("pipelines")[1] + "/jobs";
        //System.out.println("Checking pipeline if artifact " + artifactName + " exists : " + buildUrl);

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

    private JSONArray getSuccessfulPipelines(boolean limitBranch) throws IOException {
        fetchPipeline(limitBranch);
        if (limitBranch) {
            return defaultBranchPipelines;
        }
        else {
            return allPipelines;
        }
    }

    private void fetchPipeline(boolean limitBranch) throws IOException {
        if ((limitBranch && defaultBranchPipelines != null) || (!limitBranch && allPipelines != null)) {
            return;
        }
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines?status=success";
        if (limitBranch) {
            url += "&ref=" + defaultBranch;
        }
        url += "&order_by=id&sort=desc&per_page=" + numberOfVersionsToShow;
        System.out.println("Requesting successful pipelines from branch " + defaultBranch + " requiring artifact " + artifactName + " : " + url);
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

    public void deleteNonMatchingFolders(File directory, ArrayList<Pair<String, String>> retainFolderNames) throws Exception {
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

    private void downloadSelectedVersion(String selectedVersion, boolean limitBranch) throws Exception {
        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            _downloadSelectedVersion(selectedVersion, limitBranch);
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "The selected version is already downloaded. Do you want to re-download it now?",
                    "Download Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                System.out.println("Deleting Artifact " + selectedVersion);
                deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                _downloadSelectedVersion(selectedVersion, limitBranch);
            }
        }

    }


    private void _downloadSelectedVersion(String selectedVersion, boolean limitBranch) throws Exception {
        String outputFilePath = DOWNLOAD_PATH + selectedVersion + "/all-package-archive.zip";
        String extractToPath = DOWNLOAD_PATH + selectedVersion + "/";

        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            Files.createDirectories(Paths.get(DOWNLOAD_PATH + selectedVersion));
            JSONObject pipeline = findPipelineByVersion(selectedVersion, limitBranch);
            int pipelineId = pipeline.getInt("id");
            int jobId = getJobId(pipelineId, artifactName);

            if (jobId != -1) {
                try {
                    System.out.println("Downloading Artifact " + selectedVersion + " ... ");
                    downloadArtifact(jobId, outputFilePath);
                    extractZip(outputFilePath, extractToPath);
                    Files.deleteIfExists(Paths.get(outputFilePath));
                    System.out.println("Download complete");
                    populateVersions();
                    JOptionPane.showMessageDialog(this, "Downloaded and extracted: " + selectedVersion,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Exception e) {
                    deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                    throw new Exception(e);
                }
            } else {
                deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                JOptionPane.showMessageDialog(this, "Job with artifact '" + artifactName + "' not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Artifact " + selectedVersion + " already downloaded!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JSONObject findPipelineByVersion(String versionName, boolean limitBranch) throws IOException {
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

    private int getJobId(int pipelineId, String jobName) throws IOException {
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

    private void downloadArtifact(int jobId, String outputFileName) throws IOException {
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

                System.out.print("Downloading... [");

                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                    downloadedSize += count;
                    updateProgressBar(downloadedSize, fileSize, progressBarWidth);
                }

                System.out.println("\nArtifact downloaded successfully to " + outputFileName);
            }
        } else {
            throw new IOException("Failed to download artifacts: " + connection.getResponseMessage());
        }
    }

    private void updateProgressBar(long downloadedSize, long fileSize, int progressBarWidth) {
        double progress = (double) downloadedSize / fileSize * 100;
        int progressChars = (int) (progress / (100.0 / progressBarWidth));
        String progressBar = "=".repeat(progressChars);
        String emptyProgressBar = " ".repeat(progressBarWidth - progressChars);
        System.out.printf("\r[%s%s] %.2f%%", progressBar, emptyProgressBar, progress);
    }

    private void extractZip(String zipFilePath, String destDir) throws IOException {
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
        System.out.println("Artifact extracted successfully to " + destDir);
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private String getVersionNameFromDate(String dateStr) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Downloader downloader = new Downloader();
                downloader.setVisible(true);
                downloader.populateVersions(); // Fetch versions and populate UI
            }
        });
    }
}