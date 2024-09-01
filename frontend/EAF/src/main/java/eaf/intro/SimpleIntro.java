package eaf.intro;

import eaf.Main;

import javax.swing.*;
import java.awt.*;

public class SimpleIntro extends Intro {
    private JLabel objectiveLabel;

    public static boolean stop = false;

    private JFrame frame;

    public SimpleIntro() {
        // Set up the JFrame
        frame = new JFrame(Main.programName + ": Loading");
        // Load an image from file or resource
        Image icon = Toolkit.getDefaultToolkit().getImage("imgs/evoal.png");

        // Set the icon image for the frame
        frame.setIconImage(icon);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setUndecorated(true); // Remove window decorations
        frame.setLayout(new BorderLayout());

        frame.setBackground(Main.bgColor);
        setBackground(Main.bgColor);

        // Initialize the JLabel
        objectiveLabel = new JLabel("Objective: " + objective);
        objectiveLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center horizontally
        objectiveLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center vertically
        objectiveLabel.setForeground(Color.white);

        // Create a JPanel with a BorderLayout to add the JLabel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Main.bgColor);
        panel.add(objectiveLabel, BorderLayout.CENTER);

        // Add the JPanel to the frame
        frame.add(panel);

        // Center the frame on the screen
        centerFrameOnScreen(frame);

        // Make the frame visible
        frame.setVisible(true);
    }

    private void centerFrameOnScreen(JFrame frame) {
        // Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Get the frame size
        Dimension frameSize = frame.getSize();
        // Calculate the center position
        int x = (screenSize.width - frameSize.width) / 2;
        int y = (screenSize.height - frameSize.height) / 2;
        // Set the frame location
        frame.setLocation(x, y);
    }
    @Override
    public void setObjective(String objective) {
        super.setObjective(objective);
        objectiveLabel.setText("Objective: " + objective);
    }

    @Override
    public void stop() {
        stop = true;
        frame.dispose();
    }

    @Override
    public boolean isUnfinished() {
        // Implementation to check if unfinished (to be defined)
        return !stop;
    }

}
