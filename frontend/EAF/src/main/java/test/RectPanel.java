package test;

import test.rects.Rect;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RectPanel extends JScrollPane {

    public boolean drawDragging = true;

    private final ArrayList<Rect> rects = new ArrayList<>();
    public final DrawingPanel drawingPanel;
    public final DragPanel dragPanel;
    public Rect draggingRect = null;

    public static int horizontalSpacing = 10;
    public static int verticalSpacing = 5;

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        dragPanel = new DragPanel();

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(drawingPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(dragPanel, JLayeredPane.DRAG_LAYER);

        setViewportView(layeredPane);
    }

    public void addRect(Rect rect) {
        rects.add(rect);
        drawingPanel.add(rect);
        drawingPanel.revalidate();
        drawingPanel.repaint();
    }

    public void removeRect(Rect rect) {
        rects.remove(rect);
        drawingPanel.remove(rect);
        drawingPanel.revalidate();
        drawingPanel.repaint();
    }

    public ArrayList<Rect> getRects() {
        return rects;
    }

    public void setDraggingRect(Rect rect) {
        draggingRect = rect;
        dragPanel.setDraggingRect(rect);
    }

    public void clearDraggingRect() {
        dragPanel.clearDraggingRect();
        draggingRect = null;
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
            for (Rect rect : rects) {
                rect.setPosition(horizontalSpacing, y);
                rect.draw(g);
                y += rect.getHeight() + verticalSpacing;
            }

        }

        @Override
        public Dimension getPreferredSize() {
            int maxWidth = rects.stream().mapToInt(rect -> rect.getWidth()).max().orElse(0) + 20;
            int totalHeight = rects.stream().mapToInt(rect -> rect.getY() + rect.getHeight()).max().orElse(0) + 20;
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