package test.intro;

import test.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class DoubleHelixAnimation extends JPanel implements ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int RADIUS = 20;
    private static final int PERIOD = 1500; // milliseconds for one full wave
    private static final int FPS = 60;

    private double time = 0.0;
    private final Timer timer;
    private final double stopTime;
    private boolean shouldStop = false;

    private boolean unfinished = true;
    private boolean redOnTop = true; // Variable to alternate when helices are furthest apart

    private long stopMillis = -1; // To store milliseconds elapsed when the timer stops

    private final Thread repaintThread; // Thread for repainting

    public static Color c2 = new Color(203, 116, 47, 255);
    public static Color c1 = new Color(255, 255, 255, 255);

    public String objective = "";

    private static double decayTime = 1000;

    private static double extraTime = 500;

    private JFrame frame;

    public DoubleHelixAnimation(JFrame frame) {
        setDoubleBuffered(true);
        Random random = new Random();
        double randomSeconds = random.nextDouble() * 5; // Random time between 5 and 10 seconds
        this.stopTime = randomSeconds * 2 * Math.PI / (PERIOD / 1000.0); // Convert to animation time
        this.frame = frame;
        timer = new Timer(1000 / FPS, this);
        timer.start();

        // Create and start the repaint thread
        repaintThread = new Thread(() -> {
            while (true) {
                repaint();
                try {
                    Thread.sleep(1000 / FPS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        repaintThread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setStroke(new BasicStroke(4));
        // Anti-aliasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = Main.bgColor;

        double centerX = WIDTH / 2.0;
        double centerY = HEIGHT / 2.0;
        double waveLength = HEIGHT / 2.0;

        // Draw the two helices, alternating which one is on top
        drawHelix(g2d, centerX, centerY, waveLength, time);

        if (!timer.isRunning()) {
            int fontsize = 70;
            g2d.setFont(new Font("Arial", Font.PLAIN, fontsize));
            FontMetrics metrics = g2d.getFontMetrics();

            // Prepare the text and its positions
            String text1 = "E  o";
            String text2 = "Al";

            int x1 = 332;
            int x2 = x1 + metrics.stringWidth(text1);

            int x3 = x1 + metrics.stringWidth("E  oAl");



            g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(255 * progressAfterTimer())));
            int sizeOfV = 32;
            g2d.fillRect(x1, 300-fontsize, x3-x1, fontsize - sizeOfV);

            // Draw the first part of the text with color c1
            g2d.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int)(255 * progressAfterTimer())));
            g2d.drawString(text1, x1, 300);

            // Draw the second part of the text with color c2
            g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int)(255 * progressAfterTimer())));
            g2d.drawString(text2, x2, 300);

            g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(255 * progressAfterTimer())));
            g2d.fillRect(0, 0, 800, 300 - fontsize);
            g2d.fillRect(0, 301, 800, 299);


        }
        if (!shouldStop) {
            g2d.setColor(new Color (255, 255, 255, 255));
            int padding = 10;
            int fontsize = 20;
            g2d.setFont(new Font("Arial", Font.PLAIN, fontsize));
            g2d.drawString(objective, padding, padding + fontsize);
        }
    }

    public double progressAfterTimer() {
        long elapsedMillis = System.currentTimeMillis() - stopMillis;
        var r =  Math.min(1.0, elapsedMillis / decayTime);
        if (elapsedMillis >= decayTime + extraTime) {
            unfinished = false;
            frame.dispose();
        }
        return r;
    }

    public boolean isUnfinished() {
        return unfinished;
    }

    private void drawHelix(Graphics2D g2d, double centerX, double centerY, double waveLength, double time) {
        double angleIncrement = 2 * Math.PI / waveLength;

        // Variables to store the previous point for each path
        double prevX1 = centerX + RADIUS * Math.sin(time);
        double prevY1 = centerY - waveLength;
        double prevX2 = centerX + RADIUS * Math.sin(time + Math.PI);
        double prevY2 = centerY - waveLength;

        // Draw the two helices as sine waves in segments
        for (double y = -waveLength; y <= waveLength; y += 1) {
            double angle1 = angleIncrement * y + time;
            double x1 = RADIUS * Math.sin(angle1);

            double angle2 = angleIncrement * y + Math.PI + time;
            double x2 = RADIUS * Math.sin(angle2);

            double drawX1 = centerX + x1;
            double drawX2 = centerX + x2;
            double drawY = centerY + y;

            // Check if the helices are furthest apart (when the difference in X coordinates is maximized)
            double distance = Math.abs(drawX1 - drawX2);
            if (distance > RADIUS) {
                redOnTop = !redOnTop; // Alternate the color when they are furthest apart
            }


            Color cc2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue());
            if (!timer.isRunning()) {
                var rDiff = c1.getRed() - c2.getRed();
                var gDiff = c1.getGreen() - c2.getGreen();
                var bDiff = c1.getBlue() - c2.getBlue();
                cc2 = new Color(c2.getRed() + (int)(rDiff * progressAfterTimer()), c2.getGreen() + (int)(gDiff * progressAfterTimer()), c2.getBlue() + (int)(bDiff * progressAfterTimer()));
            }

            // Draw the segment from the previous point to the current point
            if (redOnTop) {
                // Red on top
                g2d.setColor(c1);
                g2d.drawLine((int) prevX1, (int) prevY1, (int) drawX1, (int) drawY);
                g2d.setColor(cc2);
                g2d.drawLine((int) prevX2, (int) prevY2, (int) drawX2, (int) drawY);
            } else {
                // Blue on top
                g2d.setColor(cc2);
                g2d.drawLine((int) prevX2, (int) prevY2, (int) drawX2, (int) drawY);
                g2d.setColor(c1);
                g2d.drawLine((int) prevX1, (int) prevY1, (int) drawX1, (int) drawY);
            }

            // Update previous points
            prevX1 = drawX1;
            prevY1 = drawY;
            prevX2 = drawX2;
            prevY2 = drawY;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        time += 2 * Math.PI / (FPS * PERIOD / 1000.0);

        // If we should stop, check if the lower meeting point is at the center
        if (shouldStop) {
            double waveLength = HEIGHT / 2.0;

            // If the lower meeting point reaches the center, stop the timer
            if (time % (2 * Math.PI) < 2 * Math.PI / (waveLength)) {
                timer.stop();
                if (stopMillis == -1) { // Capture the time when the timer stops
                    stopMillis = System.currentTimeMillis();
                }
            }
        }
    }



    public void stop() {
        shouldStop = true;
    }

    public static DoubleHelixAnimation create() {
        JFrame frame = new JFrame("Double Helix Animation");
        DoubleHelixAnimation helixAnimation = new DoubleHelixAnimation(frame);

        frame.add(helixAnimation);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Color bg = Main.bgColor;
        helixAnimation.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 255));
        return helixAnimation;
    }
}
