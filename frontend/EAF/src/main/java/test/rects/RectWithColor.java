package test.rects;

import javax.swing.*;
import java.awt.*;

public class RectWithColor extends Rect {
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
    public void onHover(Point p) {

    };

    @Override
    public void onMouseReleased() {

    };

}


