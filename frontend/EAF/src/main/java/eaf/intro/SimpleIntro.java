package eaf.intro;

import eaf.Main;
import eaf.models.ClassType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleIntro extends Intro {
    private JLabel objectiveLabel;

    public static boolean stop = false;

    private JFrame frame;


    public static BufferedImage helix = null;
    public static BufferedImage helixInv = null;

    private static Stack<ClassType> historyStack = new Stack<>();

    static {
        try {
            helix = ImageIO.read(new File("imgs/running.png"));
            helixInv = ImageIO.read(new File("imgs/running-inv.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimpleIntro() {
        // Set up the JFrame
        frame = new JFrame(Main.programName + ": Loading");
        // Load an image from file or resource
        Image icon = Toolkit.getDefaultToolkit().getImage("imgs/evoal.png");

        // Set the icon image for the frame
        frame.setIconImage(icon);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setUndecorated(true); // Remove window decorations
        frame.setLayout(new BorderLayout());

        frame.setBackground(Main.bgColor);
        setBackground(Main.bgColor);

        // Initialize the JLabel
        objectiveLabel = new JLabel("");
        objectiveLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center horizontally
        objectiveLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center vertically
        objectiveLabel.setForeground(Color.white);

        // Set the font to bold
        objectiveLabel.setFont(objectiveLabel.getFont().deriveFont(Font.BOLD));

        // Create a panel to hold the label
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(Main.bgColor); // Set background color for the panel
        labelPanel.add(objectiveLabel, BorderLayout.CENTER);

        // Set background color for the main panel
        this.setBackground(Main.bgColor);
        this.setLayout(new BorderLayout()); // Set layout for the main panel

        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 0));
        this.add(labelPanel, BorderLayout.CENTER); // Add the label panel to the main panel

        this.setBackground(Main.bgColor);
        this.add(objectiveLabel, BorderLayout.CENTER);


        // Add the JPanel to the frame
        frame.add(this);

        // Center the frame on the screen
        centerFrameOnScreen(frame);

        frame.revalidate();
        frame.repaint();

        // Make the frame visible
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double scale = 0.5;
        // Draw the image scaled to fit the panel size
        if (helix != null) {
            Dimension size = getSize();

            // Get the original dimensions of the image
            int imgWidth = helix.getWidth(null);
            int imgHeight = helix.getHeight(null);

            // Apply the scaling factor
            int newWidth = (int) (imgWidth * scale);
            int newHeight = (int) (imgHeight * scale);


            // Draw the scaled image centered in the panel
            g.drawImage(helix, 40, -30, newWidth, newHeight, this);

            g.drawImage(helixInv, 500 - newWidth - 40, -30, newWidth, newHeight, this);
        }
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
        objectiveLabel.setText(objective+ " ...");
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void stop() {
        stop = true;
        //frame.dispose();
    }

    @Override
    public boolean isUnfinished() {
        // Implementation to check if unfinished (to be defined)
        return !stop;
    }

}
