package test;

import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import compiler.SyntaxTree;
import org.json.JSONArray;
import org.json.JSONObject;
import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.TextFieldRect;
import test.rects.multi.ArrayRect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

    public RectPanel() {
        super();
        drawingPanel = new DrawingPanel();
        dragPanel = new DragPanel();


        // Set background colors for demonstration (optional)
        drawingPanel.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        drawingPanel.setForeground(DragDropRectanglesWithSplitPane.bgColor);

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
            addRect(rectFromJson((JSONObject)o));
        }
    }

    public Rect rectFromJson(JSONObject arr) {

        String clazz = (String) arr.get("sub-type");


        switch ((String) arr.get("type")) {
            case "option-field" :
                String value = (String) arr.get("value");

                var c = new ClassType(clazz, null, "Primitive");
                boolean b1 = (Boolean) arr.get("editable");
                if (clazz.contains("bool")) {
                    var r = new ArrayList<Object>();
                    r.add(true);
                    r.add(false);
                    return new OptionsFieldRect(r, value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, b1);
                }
                else {
                    return new OptionsFieldRect(DragDropRectanglesWithSplitPane.dataPanel.getDataFieldList(), value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, b1);
                }
            case "text-field" :
                String value2 = (String) arr.get("value");
                boolean b2 = (Boolean) arr.get("editable");
                var c2 = new ClassType(clazz, null, "Primitive");
                return new TextFieldRect(value2, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c2, b2);
            case "instance" :
                var reg = SyntaxTree.classRegister.get(clazz);
                String pack = (String) arr.get("package");
                if (!reg.pack.equals(pack)) {
                    System.out.println("WARNING: Unequal pack \"" + pack + "\" != \"" + reg.pack + "\" for class \"" + clazz + "\" you can ignore this if you changed the class domain.");
                }

                var v = new FieldValue(reg);
                var instance =  (ClassRect) DragDropRectanglesWithSplitPane.getRectFromClassType(v.instance);
                JSONArray value3 = (JSONArray) arr.get("value");
                int i = 0;
                for (var field : reg.fields.entrySet()) {
                    if (field.getValue().getSecond() == null) {
                        boolean found = false;
                        for (var jsonField : value3) {
                            if (((String)((JSONObject)jsonField).get("field-name")).contains(field.getKey())) {
                                instance.setIndex(i, rectFromJson((JSONObject)jsonField));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.out.println("WARNING: For field \"" + field.getKey() + "\" in class \"" + reg.name + "\" no value was found in json!");
                            if (field.getValue().getFirst().primitive) {
                                instance.setIndex(i, DragDropRectanglesWithSplitPane.getRectFromFieldType(field.getValue().getFirst(), null));
                            }
                        }
                    }
                    else {
                        if (field.getValue().getFirst().primitive) {
                            instance.setIndex(i, DragDropRectanglesWithSplitPane.getRectFromFieldType(field.getValue().getFirst(), field.getValue().getSecond()));
                        }
                    }


                    i++;
                }
                return instance;

            case "array" :


                JSONArray arrarr = (JSONArray) arr.get("value");

                var arrnames = new String[arrarr.length()];
                var arrtypes = new FieldType[arrarr.length()];
                var arrrects = new Rect[arrarr.length()];

                var ft = new FieldType(clazz, (Boolean) arr.get("primitive"), (Integer) arr.get("count"));

                int i3 = 0;
                for (int i4 = 0; i4 < arrarr.length(); i4++) {
                    arrnames[i3] = "";
                    arrtypes[i3] = ft;
                    arrrects[i3] = null;
                    i3++;
                }

                var ct = new ClassType(clazz, null, "Array");

                var arrRect = new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, ct, ft, arrnames, arrrects, arrtypes, false);

                int i2 = 0;
                for (var jsonField : arrarr) {
                    arrRect.setIndex(i2, rectFromJson((JSONObject)jsonField));
                    i2++;
                }
                return arrRect;


            default:
                throw new RuntimeException("Invalid rect type \"" + arr.get("type") + "\"!");


        }
    }

    public String toString() {
        String res = "";
        String problemContent = rects.get(0).clazz.name;;
        String problemName = rects.get(0).toString(1).split(" ", 2)[1];
        String algorithmContent = rects.get(1).clazz.name;
        String algorithmName = rects.get(1).toString(1).split(" ", 2)[1];
        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();

        /**
        for (var d : dataFieldList) {
            var c = ((DataField)d);
            if (c.isInstance()) {
                classesNeededForScript.add(SyntaxTree.classRegister.get(c.getType()));
            }
            data += "\t" + c.toFormat() + "\n";
        }**/

        res += getUniqueImports(classesNeededForScript) + "\n";
        res += "\"data\" from 'config';";
        res += "module 'config' {\n";
        res += Rect.stringPadding + "specify problem '" + problemName + "' ";
        res += problemContent;
        res += "\n";
        res += Rect.stringPadding + "configure problem '" + algorithmName + "' for '" + problemName + "' ";
        res += algorithmContent;

        res += "}";

        return res;
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