package test.rects.multi;

import compiler.ClassType;
import compiler.FieldType;
import test.rects.Rect;

import java.awt.*;

public class ClassRect extends RectWithRects {

    public ClassRect(ClassType type) {
        super(type);
    }

    public ClassRect(int width, int height, Color color, ClassType type, String[] names, FieldType[] types) {
        super(width, height, color, type, names, types);
    }

    public ClassRect(int width, int height, Color color, ClassType type, String[] names, Rect[] rects, FieldType[] types) {
        super(width, height, color, type, names, rects, types);
    }

    @Override
    public Rect clone() {
        return new ClassRect(realWidth(), realHeight(), color, getClazz(), names, subRects, types);
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
