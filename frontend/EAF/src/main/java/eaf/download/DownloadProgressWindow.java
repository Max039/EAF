package eaf.download;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DownloadProgressWindow extends JFrame {

    private JProgressBar progressBar;
    private JButton startButton;
    private Timer timer;
    private int progressValue = 0;

    public DownloadProgressWindow() {
        setTitle("Download Progress");
        setSize(400, 150);
        setLocationRelativeTo(null); // Center the window
        setUndecorated(true);

        // Create a panel to hold the components
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Initialize the progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Show progress percentage
        panel.add(progressBar, BorderLayout.CENTER);

        // Add the panel to the frame
        add(panel);

        setVisible(true);
    }

    public void setProgress(int percent) {
        progressBar.setValue(percent);
    }

    public void stop() {
        dispose();
    }

    private void startDownload() {
        progressValue = 0;
        progressBar.setValue(progressValue);
        timer.start();
    }


}
