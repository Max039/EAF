package test.rects.multi;

import test.rects.Rect;
import test.rects.RectWithColorAndTextBox;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayRect <T extends Rect> extends RectWithRects {

    boolean fillOnCreation = true;

    int buttonWidth = 5;

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
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x + width + spacing, y, buttonWidth, height); //replace with jbutton
    };

    @Override
    public int extraSpacing() {
        return spacing + buttonWidth;
    }
}
