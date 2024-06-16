package test;

import javax.swing.*;
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

            if (i == 1) {
                rightPanel.addRect(new RectWithColorAndTextBox(10, currentY, width, height, color));
            } else {
                rightPanel.addRect(new RectWithColor(10, currentY, width, height, color));
            }

            currentY += height + RECT_SPACING;
        }

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
                for (Rect rect : rightPanel.getRects()) {
                    if (rect.contains(point)) {
                        draggedRect = rect.clone();
                        dragOffset = new Point(point.x - rect.x, point.y - rect.y);
                        rightPanel.setDraggingRect(draggedRect);
                        leftPanel.setDraggingRect(draggedRect.clone());
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedRect != null) {
                    Point releasePoint = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), leftPanel);
                    if (leftPanel.contains(releasePoint)) {
                        int currentY = leftPanel.getRects().stream().mapToInt(r -> r.y + r.height + RECT_SPACING).max().orElse(0);
                        draggedRect.setPosition(10, currentY);
                        leftPanel.addRect(draggedRect);
                        rightPanel.removeRect(draggedRect);
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
        leftPanel.getViewport().getView().addMouseListener(mouseAdapter);
        leftPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
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