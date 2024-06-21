package test;

import java.awt.*;

public class ClassRect extends RectWithRects {

    public ClassRect(int width, int height, Color color, String[] names) {
        super(width, height, color, names);
    }

    public ClassRect(int width, int height, Color color, String[] names, Rect[] rects) {
        super(width, height, color, names, rects);
    }

    @Override
    public Rect clone() {
        return new ClassRect(realWidth(), realHeight(), color, names, subRects);
    }
}
