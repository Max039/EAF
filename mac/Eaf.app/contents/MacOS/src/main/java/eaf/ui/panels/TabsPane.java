package eaf.ui.panels;

import eaf.Main;
import eaf.models.ClassType;
import eaf.ui.UiUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import static eaf.Main.tabsPanel;


public class TabsPane extends JPanel implements ActionListener {

    public static BufferedImage movingImage = null;

    static {
        try {

            movingImage = ImageIO.read(new File("imgs/running.png"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static final int IMAGE_SPEED = 2; // Adjust speed as needed
    private int yOffset = 0;
    private int xSpacing = 20;
    public static BufferedImage img;

    public TabsPane(Dimension screenSize) {
        int imageHeight = movingImage.getHeight(null);
        int imageWidth = movingImage.getWidth(null);
        double scale = 0.25;

        img = scaleImage(movingImage, (int) (imageWidth * scale), (int) (imageHeight * scale));
        // Set the preferred size to fit the height of the image

        setPreferredSize(screenSize);
        setBackground(RectPanel.instanceColor);
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(true);
        Timer timer = new Timer(30, this); // Adjust delay as needed
        timer.start();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (Main.processRunning) {
            Graphics2D g2d = (Graphics2D) g;
            int imageHeight = img.getHeight(null);
            int imageWidth = img.getWidth(null);

            // Draw the image only on the right end of the panel
            for (int y = yOffset; y < getHeight(); y += imageHeight) {
                g2d.drawImage(img, getWidth() - imageWidth - xSpacing, y, this);
            }
        }
    }

    public static BufferedImage scaleImage(BufferedImage originalImage, int newWidth, int newHeight) {
        // Create a new buffered image with the desired width and height
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Get the graphics context of the new image
        Graphics2D g2d = scaledImage.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the original image scaled to the new dimensions
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);

        // Dispose of the graphics context
        g2d.dispose();

        return scaledImage;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        yOffset -= IMAGE_SPEED;
        if (yOffset <= -img.getHeight(null)) {
            yOffset = 0;
        }
        repaint();
    }
}

