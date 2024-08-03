package test;

import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import test.rects.Rect;
import test.rects.RectWithColorAndTextBox;
import test.rects.multi.ArrayRect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class RectPanel extends JScrollPane {

    public static Color arrayColor = new Color(255, 100, 100, 255);
    public static Color primitiveColor = new Color(100, 255, 100, 255);
    public static Color instanceColor = new Color(100, 100, 255, 255);

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

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        dragPanel = new DragPanel();

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(drawingPanel, JLayeredPane.DEFAULT_LAYER);

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


    public static <T extends Rect> T getRectFromClassType(ClassType type) {

        int length = type.fields.size();
        var names = new String[length];
        var types = new FieldType[length];
        var rects = new Rect[length];
        int i = 0;
        for (var field : type.fields.entrySet()) {
            names[i] = field.getKey();
            types[i] = field.getValue().getFirst();
            rects[i] = getRectFromFieldType(field.getValue().getFirst(), field.getValue().getSecond());
           i++;
        }
        return (T) new ClassRect(instanceWidth, instanceHeight, instanceColor, type, names, rects, types);
    }

    public static <T extends Rect> T getRectFromFieldType(FieldType type, FieldValue value) {

        if (value != null) {
            if (type.arrayCount > 0) {
                int length = value.values.size();
                var clazz = new ClassType(type.typeName, null, "Array");
                var names = new String[length];
                var types = new FieldType[length];
                var rects = new Rect[length];

                int i = 0;
                for (var item : value.values) {
                    names[i] = Integer.toString(i);
                    types[i] = item.type;
                    rects[i] = getRectFromFieldType(item.type, item);
                    i++;
                }

                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;
                return (T) new ArrayRect<>(arrayWidth, arrayHeight, arrayColor, clazz, ctype, names, rects, types, type.primitive);

            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    var r = new RectWithColorAndTextBox(textBoxWidth, textBoxHeight, primitiveColor, c);
                    r.setTextBox(value.value);
                    return (T) r;
                }
                else {
                    return getRectFromClassType(value.instance);
                }
            }
        }
        else {
            if (type.arrayCount > 0) {
                var clazz = new ClassType(type.typeName, null, "Array");
                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;

                return (T) new ArrayRect<>(arrayWidth, arrayHeight, arrayColor, clazz,  ctype, 1, type.primitive);
            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    return (T) new RectWithColorAndTextBox(textBoxWidth, textBoxHeight, primitiveColor, c);
                }
                else {
                    return null;
                }
            }
        }
    }


}