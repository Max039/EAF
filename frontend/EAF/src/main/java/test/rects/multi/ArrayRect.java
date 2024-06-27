package test.rects.multi;

import test.rects.Rect;
import test.rects.RectWithColorAndTextBox;

import java.awt.*;
import java.lang.reflect.Method;
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

    Class<T> clazz;
    public ArrayRect() {
        super();
        this.clazz = (Class<T> ) RectWithColorAndTextBox.class;
    }

    public ArrayRect(int width, int height, Color color, Class<T> clazz, boolean fillOnCreation) {
        super(width, height, color);
        this.clazz = clazz;
        this.fillOnCreation = fillOnCreation;
    }

    public ArrayRect(int width, int height, Color color, int num, Class<T> clazz, boolean fillOnCreation) {
        super(width, height, color);
        this.clazz = clazz;
        this.fillOnCreation = fillOnCreation;
        setNamesAndTypes(getEmptyNames(num), generateStringArray(clazz, num));
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

    public ArrayRect(int width, int height, Color color, String[] names, Rect[] rects, Class<?>[] types, Class<T> clazz, boolean fillOnCreation) {
        super(width, height, color, names, rects, types);
        this.clazz = clazz;
        this.fillOnCreation = fillOnCreation;
    }

    @Override
    public int getWidth() {
        return super.getWidth() + buttonWidth + spacing;
    }

    public String[] getEmptyNames(int num) {
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            l.add("");
        }
        return l.toArray(new String[l.size()]);
    }

    public static Class<?>[] generateStringArray(Class<?> input, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }

        Class<?>[] resultArray = new Class<?>[size];

        Arrays.fill(resultArray, input);

        return resultArray;
    }

    @Override
    public Rect clone() {
        return new ArrayRect<T>(realWidth(), realHeight(), color, names, subRects, types, clazz, fillOnCreation);
    }

    @Override
    public Rect newInstance() {
        return new ArrayRect<>();
    }

    @Override
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, height, removeButtonColor, a, ButtonType.remove);

    };

    @Override
    public int extraSpacingToRight() {
        return spacing + buttonWidth;
    }

    @Override
    public void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, emptyRowSize, addButtonColor, a, ButtonType.add);
    };

    @Override
    public int extraSpacingBelow() {
        return spacing + emptyRowSize;
    }

    private void drawButtonAt(Graphics g, int x, int y, int height, Color c, int a, ButtonType type) {
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
        g.fillRect(x, y, buttonWidth, height);

        g.setColor(new Color(buttonBorder.getRed(), buttonBorder.getGreen(), buttonBorder.getBlue(), a));
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(buttonBorderSize));
        g2.drawRect(x, y, buttonWidth, height);

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
