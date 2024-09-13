package eaf.intro;

import eaf.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DoubleHelixAnimation extends Intro implements ActionListener {
    private static int WIDTH = 0;
    private static int HEIGHT = 0;
    private static final int RADIUS = 40;
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

    public static Color c3 = new Color(120, 120, 120, 255);



    private static double decayTime = 1200;

    private static double extraTime = 850;

    private JFrame frame;

    public DoubleHelixAnimation() {
        setOpaque(false);
        setDoubleBuffered(true);
        Random random = new Random();
        double randomSeconds = random.nextDouble() * 5; // Random time between 5 and 10 seconds
        this.stopTime = randomSeconds * 2 * Math.PI / (PERIOD / 1000.0); // Convert to animation time
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

        frame = new JFrame(Main.programName + ": Loading");
        // Load an image from file or resource
        Image icon = Toolkit.getDefaultToolkit().getImage("imgs/evoal.png");

        // Set the icon image for the frame
        frame.setIconImage(icon);
        frame.setUndecorated(true);

        frame.add(this);
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

        frame.setSize(WIDTH, HEIGHT); // Assuming WIDTH and HEIGHT are 800 and 600, respectively
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Set frame background to transparent
        frame.setBackground(new Color(0, 0, 0, 0));

        // Set panel background color with transparency (alpha < 255 for partial transparency)
        Color bg = new Color(255, 255, 255, 0); // Fully transparent white
        this.setBackground(bg);

        // Set frame opacity (optional, for whole frame transparency)
        frame.setOpacity(0.9f); // Range 0.0f to 1.0f, 1.0f is fully opaque

        frame.setVisible(true);
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
        g2d.setStroke(new BasicStroke(4));
        if (!timer.isRunning()) {
            int fontsize = 70;
            g2d.setFont(new Font("Arial", Font.PLAIN, fontsize));
            FontMetrics metrics = g2d.getFontMetrics();

            // Prepare the text and its positions
            String text1 = "E  o";
            String text2 = "Al";

            int sizeOfV = 32;

            int x1 = WIDTH/2 - 68;
            int x2 = x1 + metrics.stringWidth(text1);

            int x3 = x1 + metrics.stringWidth("E  oAl");


            //g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(255 * progressAfterTimer())));

            //g2d.fillRect(x1, HEIGHT/2-fontsize, x3-x1, fontsize - sizeOfV);

            // Draw the first part of the text with color c1
            g2d.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int)(255 * progressAfterTimer())));
            g2d.drawString(text1, x1, HEIGHT/2);

            // Draw the second part of the text with color c2
            g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int)(255 * progressAfterTimer())));
            g2d.drawString(text2, x2, HEIGHT/2);

            //g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(255 * progressAfterTimer())));
            //g2d.fillRect(0, 0, WIDTH, HEIGHT/2 - fontsize);
            //g2d.fillRect(0, HEIGHT/2 + 1, WIDTH, HEIGHT/2-1);


        }
        if (!shouldStop) {
            g2d.setColor(new Color (255, 255, 255, 255));
            int padding = 25;
            int fontsize = 20;
            g2d.setFont(new Font("Arial", Font.PLAIN, fontsize));
            g2d.drawString(objective + " ...", padding, padding + fontsize);
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

        HashMap<Integer, Integer> helix1 = new HashMap<>();
        HashMap<Integer, Integer> helix2 = new HashMap<>();
        ArrayList<InnerLine> lines = new ArrayList<>();

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
            int lowerY = 0;
            int upperY = Integer.MAX_VALUE;

            Color cc2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue());
            int a = 255;
            if (!timer.isRunning()) {
                lowerY = HEIGHT/2 - 32;
                upperY = HEIGHT/2;
                var rDiff = c1.getRed() - c2.getRed();
                var gDiff = c1.getGreen() - c2.getGreen();
                var bDiff = c1.getBlue() - c2.getBlue();
                a = 255-(int)(255 * progressAfterTimer());
                cc2 = new Color(c2.getRed() + (int)(rDiff * progressAfterTimer()), c2.getGreen() + (int)(gDiff * progressAfterTimer()), c2.getBlue() + (int)(bDiff * progressAfterTimer()));
            }




            if ((Math.max(prevX1, drawX1) >= Math.min(prevX2, drawX2)) && (Math.min(prevX1, drawX1) <= Math.max(prevX2, drawX2))) {
                lines.add(new InnerLine(g2d, c3, (int) (drawY - waveLength / 10), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY - waveLength / 10 * 2), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY - waveLength / 10 * 3), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY - waveLength / 10 * 4), lowerY, upperY, a));

                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 2), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 3), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 4), lowerY, upperY, a));

                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 6), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 7), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 8), lowerY, upperY, a));
                lines.add(new InnerLine(g2d, c3, (int) (drawY + waveLength / 10 * 9), lowerY, upperY, a));
            }


            // Draw the segment from the previous point to the current point
            if (redOnTop) {
                // Red on top
                drawLineInRange(g2d, c1, (int) prevX1, (int) prevY1, (int) drawX1, (int) drawY, lowerY, upperY, a);
                drawLineInRange(g2d, cc2, (int) prevX2, (int) prevY2, (int) drawX2, (int) drawY, lowerY, upperY, a);
            } else {
                // Blue on top
                drawLineInRange(g2d, cc2, (int) prevX2, (int) prevY2, (int) drawX2, (int) drawY, lowerY, upperY, a);
                drawLineInRange(g2d, c1, (int) prevX1, (int) prevY1, (int) drawX1, (int) drawY, lowerY, upperY, a);
            }

            helix1.put((int) drawY, (int) drawX1);
            helix2.put((int) drawY, (int) drawX2);

            // Update previous points
            prevX1 = drawX1;
            prevY1 = drawY;
            prevX2 = drawX2;
            prevY2 = drawY;
        }
        g2d.setStroke(new BasicStroke(2));
        for (var a : lines) {
            if (helix1.get(a.y) != null && helix2.get(a.y) != null) {
                a.draw(helix1.get(a.y), helix2.get(a.y));
            }
        }
    }


    public static void drawLineInRange(Graphics2D g2d, Color c, int x1, int y1, int x2, int y2, int upperY, int lowerY, int a) {
        g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));


        g2d.setColor(c);
        // Check if both points are within the Y range
        boolean y1InRange = y1 >= upperY && y1 <= lowerY;
        boolean y2InRange = y2 >= upperY && y2 <= lowerY;

        if (y1InRange && y2InRange) {
            // Both points are in the range, draw the entire line
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        } else if (y1InRange || y2InRange) {
            // Only one point is in the range, calculate the intersection
            double xIntersect1 = x1;
            double yIntersect1 = y1;
            double xIntersect2 = x2;
            double yIntersect2 = y2;

            if (!y1InRange) {
                // y1 is out of range, calculate intersection with upper or lower Y
                if (y1 < upperY) {
                    xIntersect1 = x1 + (upperY - y1) * (x2 - x1) / (y2 - y1);
                    yIntersect1 = upperY;
                } else if (y1 > lowerY) {
                    xIntersect1 = x1 + (lowerY - y1) * (x2 - x1) / (y2 - y1);
                    yIntersect1 = lowerY;
                }
            }

            if (!y2InRange) {
                // y2 is out of range, calculate intersection with upper or lower Y
                if (y2 < upperY) {
                    xIntersect2 = x2 + (upperY - y2) * (x1 - x2) / (y1 - y2);
                    yIntersect2 = upperY;
                } else if (y2 > lowerY) {
                    xIntersect2 = x2 + (lowerY - y2) * (x1 - x2) / (y1 - y2);
                    yIntersect2 = lowerY;
                }
            }

            // Draw the line segment between the calculated intersection points
            g2d.draw(new Line2D.Double(xIntersect1, yIntersect1, xIntersect2, yIntersect2));
        }
        // If neither point is in range, do nothing
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

}
