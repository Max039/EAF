package test.rects;

import test.Pair;
import test.rects.multi.ArrayRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;

public class RectWithColor extends Rect {

    public RectWithColor() {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255));
    }

    private RectWithColor(int width, int height, Color color) {
        super(width, height, color);
    }

    public static RectWithColor createRectWithColor(int width, int height, Color color) {
        return new RectWithColor(width, height, color);
    }

    @Override
    public void draw(Graphics g, double a) {
        var g2 = (Graphics2D) g;

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public Rect clone() {
        return createRectWithColor(getWidth(), getHeight(), color);
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
    public Rect newInstance() {
        return new RectWithColor();
    }



}


