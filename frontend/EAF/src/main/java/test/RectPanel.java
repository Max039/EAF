package test;

import compiler.ClassType;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class RectPanel extends JScrollPane {

    public static Color arrayColor = new Color(43, 43, 43, 255);
    public static Color primitiveColor = new Color(50, 50, 50, 255);
    public static Color instanceColor = new Color(43, 43, 43, 255);

    public static int textBoxWidth = 40;

    public static int textBoxHeight = 20;

    public static int arrayWidth = 40;

    public static int arrayHeight = RectWithRects.spacing * 2;

    public static int instanceWidth = 40;

    public static int instanceHeight = 40;

    public boolean drawDragging = true;

    private final ArrayList<Rect> rects = new ArrayList<>();
    public final DrawingPanel drawingPanel;
    public final DragPanel dragPanel;

    public final JLayeredPane layeredPane;

    public Rect draggingRect = null;

    public static int horizontalSpacing = 10;
    public static int verticalSpacing = 5;

    public String filter = "";

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        dragPanel = new DragPanel();


        // Set background colors for demonstration (optional)
        drawingPanel.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        drawingPanel.setForeground(DragDropRectanglesWithSplitPane.bgColor);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(drawingPanel, JLayeredPane.DEFAULT_LAYER);

        this.setBorder(BorderFactory.createEmptyBorder());
        layeredPane.setBorder(BorderFactory.createEmptyBorder());
        drawingPanel.setBorder(BorderFactory.createEmptyBorder());

        setViewportView(layeredPane);

    }

    public void addRect(Rect rect) {
        rects.add(rect);
        drawingPanel.add(rect);
        revalidate();
        repaint();
    }

    public void removeRect(Rect rect) {
        rects.remove(rect);
        drawingPanel.remove(rect);
        revalidate();
        repaint();
    }

    public ArrayList<Rect> getRects() {
        return rects;
    }

    public void setDraggingRect(Rect rect) {
        draggingRect = rect;
        layeredPane.add(dragPanel, JLayeredPane.DRAG_LAYER);
        dragPanel.setDraggingRect(rect);
        revalidate();
        repaint();
    }

    public void clearDraggingRect() {
        dragPanel.clearDraggingRect();
        draggingRect = null;
        layeredPane.remove(dragPanel);
        revalidate();
        repaint();
    }

    public Rect getRect(Point p) {
        for (Rect r : rects) {
            if (r.contains(p)) {
                return r;
            }
        }
        return null;
    }

    public void mouseReleased() {
        for (Rect rect : rects) {
            rect.onMouseReleased();
        }
    }

    @Override
    public void repaint() {
        if (drawingPanel != null) {
            drawingPanel.repaint();
        }
        super.repaint();

    }

    @Override
    public void revalidate() {
        if (drawingPanel != null) {
            drawingPanel.revalidate();
        }
        super.revalidate();

    }


    public void setRects(ArrayList<ClassType> cs) {
        ArrayList<Rect> r2 = (ArrayList<Rect>) rects.clone();
        for (var r : r2) {
            removeRect(r);
        }
        for (var c : cs) {
            addRect(DragDropRectanglesWithSplitPane.getRectFromClassType(c));
        }
    }


    class DrawingPanel extends JPanel {

        public DrawingPanel() {
            setLayout(null);

        }

        public void add(Rect rect) {
            rect.addTo(this);
        }

        public void remove(Rect rect) {
            rect.removeFrom(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int y = verticalSpacing;

            Optional<Integer> minSpac = rects.stream().map(t -> (this.getWidth() - t.getWidth()) / 2).min(Integer::compareTo);
            for (Rect rect : rects) {
                if (filter.isEmpty()) {
                    rect.setPosition(minSpac.get(), y);
                    rect.draw(g);
                    y += rect.getHeight() + verticalSpacing;
                }
                else {
                    var parts = filter.split(" ");
                    boolean found = true;
                    for (var part : parts) {
                        if (!rect.clazz.name.toLowerCase().contains(part.toLowerCase())) {
                            found = false;
                            break;
                        }
                    }

                    if (found) {
                        rect.setPosition(horizontalSpacing, y);
                        rect.draw(g);
                        y += rect.getHeight() + verticalSpacing;
                    }
                    else {
                        rect.setPosition(-50000, -50000);
                        rect.draw(g);
                    }
                }

            }
        }

        @Override
        public Dimension getPreferredSize() {
            int maxWidth = rects.stream().mapToInt(Rect::getWidth).max().orElse(0) + horizontalSpacing * 2;
            int totalHeight = rects.stream().mapToInt(rect -> rect.getY() + rect.getHeight()).max().orElse(0) + verticalSpacing * 2;
            return new Dimension(maxWidth, totalHeight);
        }
    }

    class DragPanel extends JPanel {
        private Rect draggingRect = null;

        public DragPanel() {
            setOpaque(false);

        }

        public void add(Rect rect) {
            rect.addTo(this);
        }

        public void remove(Rect rect) {
            rect.removeFrom(this);
        }

        public void setDraggingRect(Rect rect) {
            draggingRect = rect;
            add(draggingRect);
        }

        public void clearDraggingRect() {
            remove(draggingRect);
            draggingRect = null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (draggingRect != null && drawDragging) {
                draggingRect.draw(g);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return drawingPanel.getPreferredSize();
        }
    }


}