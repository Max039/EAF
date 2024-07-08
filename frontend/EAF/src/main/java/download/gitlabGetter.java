package download;

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


public class gitlabGetter {

    private static final String GITLAB_URL = "https://gitlab.informatik.uni-bremen.de/api/v4";
    private static final String PROJECT_ID = "evoal%2Fsource%2Fevoal-core"; // URL-encoded project ID
    private static final String PRIVATE_TOKEN = "oMAm4zMJVy9xc35PxQZg"; // Replace with your personal access token

    private static final String DOWNLOAD_PATH = "evoalBuild/";


    public static void main(String[] args) {
        try {
            JSONObject latestPipeline = getLatestSuccessfulPipeline();
            int pipelineId = latestPipeline.getInt("id");
            String updatedAt = latestPipeline.getString("updated_at");
            String versionName = getVersionNameFromDate(updatedAt);
            String outputFilePath = DOWNLOAD_PATH + versionName + "/all-package-archive.zip";
            String extractToPath = DOWNLOAD_PATH + versionName + "/";

            if (!Files.exists(Paths.get(outputFilePath))) {
                Files.createDirectories(Paths.get(DOWNLOAD_PATH + versionName));
                int jobId = getJobId(pipelineId, "all:package");
                if (jobId != -1) {
                    downloadArtifact(jobId, outputFilePath);
                    extractZip(outputFilePath, extractToPath);
                    System.out.println("Deleting zip...");
                    File file = new File(outputFilePath);
                    file.delete();
                } else {
                    System.out.println("Job with artifact 'all:package' not found.");
                }
            } else {
                System.out.println("The latest version is already downloaded.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getLatestSuccessfulPipeline() throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines?status=success&order_by=id&sort=desc&per_page=1";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                JSONArray pipelines = new JSONArray(response);
                if (pipelines.length() > 0) {
                    return pipelines.getJSONObject(0);
                } else {
                    throw new IOException("No successful pipelines found.");
                }
            }
        } else {
            throw new IOException("Failed to get pipelines: " + connection.getResponseMessage());
        }
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

    private static void downloadArtifact(int jobId, String outputFileName) throws IOException {
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
        System.out.println("Artifact extracted successfully to " + destDir);
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
}