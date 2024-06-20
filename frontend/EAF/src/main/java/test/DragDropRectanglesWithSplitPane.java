package test;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class DragDropRectanglesWithSplitPane extends JPanel {

    private static final int RECT_SPACING = 5;
    private final RectPanel leftPanel = new RectPanel();
    private final RectPanel rightPanel = new RectPanel();
    public Rect draggedRect = null;
    private Point dragOffset = null;

    private JSplitPane splitPane = null;

    public DragDropRectanglesWithSplitPane(int numRects) {
        setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400); // Initial divider location
        splitPane.setResizeWeight(0.5); // Evenly split the panels
        add(splitPane, BorderLayout.CENTER);

        int currentY = 0;
        Random random = new Random();
        for (int i = 0; i < numRects; i++) {
            int width = random.nextInt(100) + 50;
            int height = random.nextInt(50) + 30;
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Rect r;

            if (i == 1) {
                r = new RectWithColorAndTextBox(10, currentY, width, height, color);

            } else if (i == 2) {
                r = new RectWithRects(10, currentY, width, height, color, new String[]{"1", "2"});
            }
            else {
                r = new RectWithColor(10, currentY, width, height, color);
            }
            rightPanel.addRect(r);
            currentY += r.getHeight() + RECT_SPACING;
        }

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());

                Rect rect = rightPanel.getRect(point);

                if (rect != null && rect.contains(point)) {
                    draggedRect = rect.clone();
                    dragOffset = new Point(point.x - rect.getX(), point.y - rect.getY());
                    rightPanel.setDraggingRect(draggedRect);
                    leftPanel.setDraggingRect(draggedRect.clone());

                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedRect != null) {
                    Point releasePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
                    Point pointFromLeft = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel);
                    if (leftPanel.contains(pointFromLeft)) {
                        Rect matchingRect = leftPanel.getRect(releasePoint);
                        if (matchingRect instanceof RectWithRects) {
                            if (((RectWithRects)matchingRect).setIndex(releasePoint, draggedRect)) {
                                rightPanel.removeRect(draggedRect);
                                draggedRect.addTo(leftPanel.drawingPanel);
                                leftPanel.revalidate();
                                leftPanel.repaint();
                            }
                        }
                        else {
                            int currentY = leftPanel.getRects().stream().mapToInt(r -> r.getY() + r.getHeight() + RECT_SPACING).max().orElse(0);
                            draggedRect.setPosition(10, currentY);
                            leftPanel.addRect(draggedRect);
                            rightPanel.removeRect(draggedRect);
                        }

                    }
                    draggedRect = null;
                    dragOffset = null;
                    leftPanel.clearDraggingRect();
                    rightPanel.clearDraggingRect();
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedRect != null) {
                    Point rightPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
                    Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
                    int newX = rightPanelPos.x - dragOffset.x;
                    int newY = rightPanelPos.y - dragOffset.y;
                    leftPanel.draggingRect.setPosition(newX + leftPanel.getWidth() + splitPane.getDividerSize(), leftPanelPos.y  - dragOffset.y);
                    rightPanel.draggingRect.setPosition(newX, newY);
                    repaint();
                }
            }
        };

        rightPanel.getViewport().getView().addMouseListener(mouseAdapter);
        rightPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
        //leftPanel.getViewport().getView().addMouseListener(mouseAdapter);
        //leftPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
    }

    private static void createAndShowGUI(int numRects) {
        JFrame frame = new JFrame("Drag and Drop Rectangles with Split Pane");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DragDropRectanglesWithSplitPane dragDropRectangles = new DragDropRectanglesWithSplitPane(numRects);
        frame.add(dragDropRectangles);
        frame.setSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        int numRects = 20;  // Change this number to create more or fewer rectangles
        SwingUtilities.invokeLater(() -> createAndShowGUI(numRects));
    }
}