package test.rects.multi;

import compiler.ClassType;
import compiler.FieldType;
import test.DragDropRectanglesWithSplitPane;
import test.rects.Rect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

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

    FieldType fillType;

    public ArrayRect(ClassType type) {
        super(type);
    }

    public ArrayRect(int width, int height, Color color, ClassType type, FieldType field, boolean fillOnCreation) {
        super(width, height, color, type);
        this.fillOnCreation = fillOnCreation;
        this.fillType = field;
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

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

}
