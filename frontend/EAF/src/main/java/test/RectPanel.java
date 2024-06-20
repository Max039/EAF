package test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class RectPanel extends JScrollPane {

    private final ArrayList<Rect> rects = new ArrayList<>();
    public final DrawingPanel drawingPanel;
    public Rect draggingRect = null;

    public static int horizontalSpacing = 10;

    public static int verticalSpacing = 5;

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        setViewportView(drawingPanel);
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
        drawingPanel.setDraggingRect(rect);
    }

    public void clearDraggingRect() {
        drawingPanel.clearDraggingRect();
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
        private Rect draggingRect = null;

        public DrawingPanel() {
            setLayout(null);
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
            int y = verticalSpacing;
            for (Rect rect : rects) {
                rect.setPosition(horizontalSpacing, y);
                rect.draw(g);
                y += rect.getHeight() + verticalSpacing;
            }
            if (draggingRect != null) {
                draggingRect.draw(g);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int maxWidth = rects.stream().mapToInt(rect -> rect.getWidth()).max().orElse(0) + 20;
            int totalHeight = rects.stream().mapToInt(rect -> rect.getY() + rect.getHeight()).max().orElse(0) + 20;
            return new Dimension(maxWidth, totalHeight);
        }
    }
}