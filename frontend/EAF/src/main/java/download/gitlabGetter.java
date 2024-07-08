package download;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class gitlabGetter {

    private static final String GITLAB_URL = "https://gitlab.informatik.uni-bremen.de/api/v4";
    private static final String PROJECT_ID = "evoal%2Fsource%2Fevoal-core"; // URL-encoded project ID
    private static final String PRIVATE_TOKEN = "oMAm4zMJVy9xc35PxQZg"; // Replace with your personal access token

    public static void main(String[] args) {
        try {
            int pipelineId = getLatestSuccessfulPipelineId();
            printJobsDetails(pipelineId);
            int jobId = getJobId(pipelineId, "all:package");
            if (jobId != -1) {
                downloadArtifact(jobId, "all-package-archive.zip");
            } else {
                System.out.println("Job with artifact 'all:package' not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getLatestSuccessfulPipelineId() throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines?status=success&order_by=id&sort=desc&per_page=1";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                return Integer.parseInt(response.split("\"id\":")[1].split(",")[0].trim());
            }
        } else {
            throw new IOException("Failed to get pipelines: " + connection.getResponseMessage());
        }
    }

    private static void printJobsDetails(int pipelineId) throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines/" + pipelineId + "/jobs";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println("Jobs in the latest successful pipeline:");
                System.out.println(response);
            }
        } else {
            throw new IOException("Failed to get jobs: " + connection.getResponseMessage());
        }
    }

    private static int getJobId(int pipelineId, String jobName) throws IOException {
        String url = GITLAB_URL + "/projects/" + PROJECT_ID + "/pipelines/" + pipelineId + "/jobs";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                String[] jobs = response.split("\\},\\{");
                for (String job : jobs) {
                    if (job.contains("\"name\":\"" + jobName + "\"")) {
                        return Integer.parseInt(job.split("\"id\":")[1].split(",")[0].trim());
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
            }
        } else {
            throw new IOException("Failed to download artifacts: " + connection.getResponseMessage());
        }
    }
}