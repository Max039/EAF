package test.rects.multi;

import compiler.ClassType;
import compiler.FieldType;
import org.json.JSONArray;
import org.json.JSONObject;
import test.DragDropRectanglesWithSplitPane;
import test.Pair;
import test.rects.OptionsFieldRect;
import test.rects.Rect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ArrayRect <T extends Rect> extends RectWithRects {

    Color removeButtonColor = new Color(230, 70, 70);

    Color addButtonColor = new Color(220, 220, 220);

    Color buttonBorder = new Color(0, 0, 0);

    private static int buttonBorderSize = 1;

    boolean fillOnCreation = true;

    int buttonWidth = 12;

    enum ButtonType {
        add,
        remove
    }

    public FieldType fillType;

    public ArrayRect(int width, int height, Color color, ClassType type, FieldType field, int num, boolean fillOnCreation) {
        super(width, height, color, type);
        this.fillOnCreation = fillOnCreation;
        this.fillType = field;
        setNamesAndTypes(getEmptyNames(num), generateStringArray(field, num));
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

    public ArrayRect(int width, int height, Color color, ClassType type, FieldType field, String[] names, Rect[] rects, FieldType[] types, boolean fillOnCreation) {
        super(width, height, color, type, names, rects, types);
        this.fillOnCreation = fillOnCreation;
        this.fillType = field;
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

    @Override
    public int getWidth() {
        if (DragDropRectanglesWithSplitPane.showButtons) {
            return super.getWidth() + buttonWidth + spacing;
        }
        else {
            return super.getWidth();
        }
    }

    public String[] getEmptyNames(int num) {
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            l.add("");
        }
        return l.toArray(new String[l.size()]);
    }

    public static FieldType[] generateStringArray(FieldType input, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }

        FieldType[] resultArray = new FieldType[size];

        Arrays.fill(resultArray, input);

        return resultArray;
    }

    @Override
    public Rect clone() {
        System.out.println("array");
        return new ArrayRect<T>(realWidth(), realHeight(), color, getClazz(), fillType, names, subRects, types, fillOnCreation);
    }


    @Override
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, height, removeButtonColor, a, ButtonType.remove);

    };

    @Override
    public void drawOnTop(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public int extraSpacingToRight() {
        if (DragDropRectanglesWithSplitPane.showButtons) {
            return spacing + buttonWidth;
        }
        else {
            return 0;
        }
    }

    @Override
    public void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, emptyRowSize, addButtonColor, a, ButtonType.add);
    };

    @Override
    public int extraSpacingBelow() {
        if (DragDropRectanglesWithSplitPane.showButtons) {
            return spacing + emptyRowSize;
        }
        else {
            return 0;
        }
    }

    private void drawButtonAt(Graphics g, int x, int y, int height, Color c, int a, ButtonType type) {
        if (DragDropRectanglesWithSplitPane.showButtons) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
            g.fillRect(x, y, buttonWidth, height);

            g.setColor(new Color(buttonBorder.getRed(), buttonBorder.getGreen(), buttonBorder.getBlue(), a));
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(buttonBorderSize));
            g2.drawRect(x, y, buttonWidth, height);
        }

        /**
        if (type == ButtonType.remove) {
            g2.drawLine(x, y, x + buttonWidth, y + height);
            g2.drawLine(x + buttonWidth, y, x, y + height);
        }
        else {
            g2.drawLine(x + buttonWidth/2, y, x + buttonWidth/2, y + height);
            g2.drawLine(x, y + height/2, x  + buttonWidth, y + height/2);
        }
         **/
    }

    @Override
    public String toString(int level) {
        String res = "";
        res += "[";
        boolean test = Arrays.stream(subRects).anyMatch(Objects::nonNull);
        if (test) {
            res += "\n";
        }

        Rect[] subRects2 = Arrays.stream(subRects).filter(Objects::nonNull).filter(t -> !(t instanceof OptionsFieldRect) || !((String) (((OptionsFieldRect) (t)).comboBox.getSelectedItem())).isEmpty()).toArray(Rect[]::new);
        for (int i = 0; i < subRects2.length; i++) {
            var res2 = repeatString(stringPadding, level + 1 ) + subRects2[i].toString(level + 1);
            res += res2;
            if (i < subRects2.length - 1) {
                res += ",\n";
            }
            else {
                res += "\n";
            }
        }
        if (test) {
            res += repeatString(stringPadding, level);
        }
        res += "]";
        return res;
    }

    public void removeElement(int index) {


        // Check if the index is valid
        if (index < 0 || index >= subRects.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }

        Rect[] newRects = new Rect[subRects.length - 1];
        String[] newNames = new String[subRects.length - 1];
        FieldType[] newTypes = new FieldType[subRects.length - 1];

        // Copy elements from the original array to the new array
        for (int i = 0, j = 0; i < subRects.length; i++) {
            if (i != index) {
                newRects[j] = subRects[i];
                newNames[j] = names[i];
                newTypes[j++] = types[i];
            }
            else {
                if (subRects[i] != null) {
                    DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(subRects[i]);
                }
            }
        }

        subRects =  newRects;
        names =  newNames;
        types =  newTypes;
    }

    public void addElement() {
        System.out.println("Name = " + names.length);
        System.out.println("Rect = " + subRects.length);
        System.out.println("Types = " + types.length);

        Rect[] newRects = new Rect[subRects.length + 1];
        String[] newNames = new String[subRects.length + 1];
        FieldType[] newTypes = new FieldType[subRects.length + 1];

        for (int i = 0; i < subRects.length; i++) {
            newRects[i] = subRects[i];
            newNames[i] = names[i];
            newTypes[i] = types[i];
        }
        subRects =  newRects;
        names =  newNames;
        types =  newTypes;

        names[names.length - 1] = "";
        types[types.length - 1] = fillType;

        if (this.fillOnCreation) {
            fillIfNecessary();
        }

        if (subRects[subRects.length - 1] != null) {
            DragDropRectanglesWithSplitPane.subFrame.leftPanel.addRect(subRects[subRects.length - 1]);
        }
    }

    public Pair<Boolean, Integer> getArrayButton(Point p) {
        int heightAcc = realHeight();
        if (p.x >= getX() + getWidth() - spacing - buttonWidth && p.x <= getX() + getWidth() - spacing) {
            for (int i = 0; i < subRects.length + 1 && getY() + heightAcc <= p.y; i++) {
                if (i < subRects.length) {
                    Rect r = subRects[i];
                    if (r != null) {
                        if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + r.getHeight()) {
                            return new Pair<Boolean, Integer>(true, i);
                        }
                        heightAcc += r.getHeight() + spacing * 2;
                    }
                    else {
                        if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                            return new Pair<Boolean, Integer>(true, i);
                        }
                        heightAcc += emptyRowSize + spacing * 2;
                    }
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                        return new Pair<Boolean, Integer>(true, -1);
                    }
                }
            }
        }
        return new Pair<Boolean, Integer>(false, -1);
    };

    public boolean pressedButton(Point p) {
        var res = getArrayButton(p);
        System.out.println(res.getFirst() + " " + res.getSecond());
        if (res.getFirst()) {
            if (res.getSecond() == -1) {
                addElement();
            }
            else {
                removeElement(res.getSecond());
            }

        }
        DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
        DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
        return res.getFirst();
    }

}
