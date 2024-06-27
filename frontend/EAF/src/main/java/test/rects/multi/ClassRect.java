package test.rects.multi;

import test.rects.Rect;

import java.awt.*;

public class ClassRect extends RectWithRects {

    public ClassRect() {
        super();
    }

    public ClassRect(int width, int height, Color color, String[] names, Class<?>[] types) {
        super(width, height, color, names, types);
    }

    public ClassRect(int width, int height, Color color, String[] names, Rect[] rects, Class<?>[] types) {
        super(width, height, color, names, rects, types);
    }

    @Override
    public Rect clone() {
        return new ClassRect(realWidth(), realHeight(), color, names, subRects, types);
    }

    @Override
    public Rect newInstance() {
        return new ClassRect();
    }

    @Override
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public int extraSpacingToRight() {
        return 0;
    }

    @Override
    public void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public int extraSpacingBelow() {
        return 0;
    }
}
