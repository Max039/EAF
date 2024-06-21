package test;

import java.awt.*;
import java.util.ArrayList;

public class ArrayRect extends RectWithRects {

    public ArrayRect(int width, int height, Color color) {
        super(width, height, color);
    }

    public ArrayRect(int width, int height, Color color, int num) {
        super(width, height, color);
        setNames(getEmptyNames(num));
    }

    public ArrayRect(int width, int height, Color color, String[] names, Rect[] rects) {
        super(width, height, color, names, rects);
    }


    public String[] getEmptyNames(int num) {
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            l.add("");
        }
        return l.toArray(new String[l.size()]);
    }

    @Override
    public Rect clone() {
        return new ArrayRect(realWidth(), realHeight(), color, names, subRects);
    }

}
