package test.rects;

import compiler.ClassType;
import test.Pair;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;

public class RectWithColor extends Rect {

    public RectWithColor(ClassType type) {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255), type);
    }

    private RectWithColor(int width, int height, Color color, ClassType type) {
        super(width, height, color, type);
    }

    public static RectWithColor createRectWithColor(int width, int height, Color color, ClassType type) {
        return new RectWithColor(width, height, color, type);
    }

    @Override
    public void draw(Graphics g, double a) {
        var g2 = (Graphics2D) g;

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public Rect clone() {
        return createRectWithColor(getWidth(), getHeight(), color, clazz);
    }

    @Override
    public void addTo(JPanel p) {

    }

    @Override
    public void removeFrom(JPanel p) {

    }

    @Override
    public Pair<Boolean, Boolean> onHover(Point p) {
        return new Pair<>(true, false);
    };

    @Override
    public void onMouseReleased() {

    };

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2) {

    };


}


