package test;

import compiler.ClassType;
import org.json.JSONArray;
import org.json.JSONObject;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static compiler.ClassType.getUniqueImports;


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

    public int lastMatchedCount = 0;

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        dragPanel = new DragPanel();


        // Set background colors for demonstration (optional)
        drawingPanel.setBackground(Main.bgColor);
        drawingPanel.setForeground(Main.bgColor);

        layeredPane = new JLayeredPane() {
            @Override
            public Dimension getPreferredSize() {
                return drawingPanel.getPreferredSize();
            }
        };
        layeredPane.add(drawingPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        drawingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        layeredPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        dragPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));



        this.setBorder(BorderFactory.createEmptyBorder());
        layeredPane.setBorder(BorderFactory.createEmptyBorder());
        drawingPanel.setBorder(BorderFactory.createEmptyBorder());

        setViewportView(layeredPane);

    }

    public boolean hasRect(Rect rect) {
        for (var r : rects) {
            if (isInRect(r, rect)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInRect(Rect rect, Rect toTest) {
        if (rect == toTest) {
            return true;
        }
        if (rect instanceof RectWithRects) {
            var r = (RectWithRects) rect;
            for (var subr : r.getSubRects()) {
                if (isInRect(subr, toTest)) {
                    return true;
                }
            }
        }

        return false;
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
        if (Main.subFrame != null) {
            InputHandler.stringMarker = new HashMap<>();
        }
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
            addRect(RectFactory.getRectFromClassType(c));
        }
    }

    public JSONArray toJson() {
        JSONArray rts = new JSONArray();
        for (var r : rects) {
                rts.put(r.toJson());
        }
        return rts;
    }

    public void fromJson(JSONArray arr) {
        var rc = (ArrayList<Rect>) rects.clone();
        for (var r : rc) {
            removeRect(r);
        }
        rects.clear();

        for (var o : arr) {
            addRect(RectFactory.rectFromJson((JSONObject)o));
        }
    }

    public ArrayList<ClassType> getAllRects(ArrayList<ClassType> t, Rect r) {
        var c = r.clazz;
        if (!c.pack.equals("Array") && !c.pack.equals("Primitive")) {
            t.add(c);
        }

        if (r instanceof RectWithRects) {
            var rr = (RectWithRects) r;
            for (var sr : rr.getSubRects()) {
                getAllRects(t, sr);
            }
        }
        return t;
    }

    public String toString() {
        String res = "";
        String problemName = rects.get(0).clazz.name;;
        String problemContent = rects.get(0).toString(1).split(" ", 2)[1];
        String algorithmName = rects.get(1).clazz.name;
        String algorithmContent = rects.get(1).toString(1).split(" ", 2)[1];
        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();
        for (var r : rects) {
            getAllRects(classesNeededForScript, r);
        }

        res += getUniqueImports(classesNeededForScript) + "\n";
        res += "import \"data\" from 'config';\n\n";
        res += "module 'config' {\n";
        res += Rect.stringPadding + "specify problem '" + problemName + "' ";
        res += problemContent;
        res += "\n\n\n";
        res += Rect.stringPadding + "configure problem '" + algorithmName + "' for '" + problemName + "' ";
        res += algorithmContent;

        res += "}";

        return res;
    }

    public Long getMatchingRects() {
        return rects.stream().filter(s -> {
            var parts = filter.split(" ");
            boolean found = true;
            for (var part : parts) {
                if (!s.clazz.name.toLowerCase().contains(part.toLowerCase())) {
                    found = false;
                    break;
                }
            }
            return found;
        }).count();
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
                        rect.setPosition(minSpac.get(), y);
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