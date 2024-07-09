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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;


public class gitlabGetter extends JFrame {

    private static final String GITLAB_URL = "https://gitlab.informatik.uni-bremen.de/api/v4";
    private static final String PROJECT_ID = "evoal%2Fsource%2Fevoal-core"; // URL-encoded project ID
    private static final String PRIVATE_TOKEN = "oMAm4zMJVy9xc35PxQZg"; // Replace with your personal access token

    private static final String DOWNLOAD_PATH = "evoalBuild/";

    private static final String branchToConsider = "develop";

    private static int numberOfVersionsToShow = 20;

    private JComboBox<String> versionComboBox;
    private JButton downloadButton;

    public gitlabGetter() {
        setTitle("Artifact Downloader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        versionComboBox = new JComboBox<>();
        panel.add(versionComboBox, BorderLayout.CENTER);

        downloadButton = new JButton("Download Selected Version");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedVersion = (String) versionComboBox.getSelectedItem();
                if (selectedVersion != null) {
                    selectedVersion = selectedVersion.split(" ")[0];
                    try {
                        downloadSelectedVersion(selectedVersion);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(gitlabGetter.this,
                                "Failed to download artifact: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        panel.add(downloadButton, BorderLayout.SOUTH);

        add(panel);
    }

    private void populateVersions() {
        try {
            JSONArray pipelines = getSuccessfulPipelines();
            System.out.println("Retrieved " + pipelines.length() + " successful pipelines. Current limit for successful pipelines is set to: " + numberOfVersionsToShow );
            for (int i = 0; i < pipelines.length(); i++) {
                JSONObject pipeline = pipelines.getJSONObject(i);
                String updatedAt = pipeline.getString("updated_at");
                String versionName = getVersionNameFromDate(updatedAt);
                String s = versionName;

                if (Files.exists(Paths.get(DOWNLOAD_PATH + versionName))) {
                    s += " (downloaded)";
                }
                if ( i == 0 ) {
                    s += " (newest)";
                }
                versionComboBox.addItem(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch versions: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JSONArray getSuccessfulPipelines() throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines?status=success&ref=" + branchToConsider + "&order_by=id&sort=desc&per_page=" + numberOfVersionsToShow;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                return new JSONArray(response);
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


    private void downloadSelectedVersion(String selectedVersion) throws Exception {
        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            _downloadSelectedVersion(selectedVersion);
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "The selected version is already downloaded. Do you want to re-download it now?",
                    "Download Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                System.out.println("Deleting Artifact " + selectedVersion);
                deleteDirectory(new File(DOWNLOAD_PATH + selectedVersion));
                _downloadSelectedVersion(selectedVersion);
            }
        }

    }

    private void _downloadSelectedVersion(String selectedVersion) throws IOException {
        String outputFilePath = DOWNLOAD_PATH + selectedVersion + "/all-package-archive.zip";
        String extractToPath = DOWNLOAD_PATH + selectedVersion + "/";

        if (!Files.exists(Paths.get(DOWNLOAD_PATH + selectedVersion))) {
            Files.createDirectories(Paths.get(DOWNLOAD_PATH + selectedVersion));
            JSONObject pipeline = findPipelineByVersion(selectedVersion);
            int pipelineId = pipeline.getInt("id");
            int jobId = getJobId(pipelineId, "all:package");

            if (jobId != -1) {
                System.out.println("Downloading Artifact " + selectedVersion + " ... ");
                downloadArtifact(jobId, outputFilePath);
                extractZip(outputFilePath, extractToPath);
                Files.deleteIfExists(Paths.get(outputFilePath));
                System.out.println("Download complete");
                JOptionPane.showMessageDialog(this, "Downloaded and extracted: " + selectedVersion,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Job with artifact 'all:package' not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Artifact " + selectedVersion + " already downloaded!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JSONObject findPipelineByVersion(String versionName) throws IOException {
        JSONArray pipelines = getSuccessfulPipelines();
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
                byte[] buffer = new byte[1024];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                System.out.println("Artifact downloaded successfully to " + outputFileName);
            }
        } else {
            throw new IOException("Failed to download artifacts: " + connection.getResponseMessage());
        }
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
                gitlabGetter downloader = new gitlabGetter();
                downloader.setVisible(true);
                downloader.populateVersions(); // Fetch versions and populate UI
            }
        });
    }
}