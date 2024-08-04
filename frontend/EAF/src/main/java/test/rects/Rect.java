package test.rects;

import compiler.ClassType;
import test.Pair;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class Rect {

    public ClassType getClazz() {
        return clazz;
    }

    public ClassType clazz;

    public static double transparencyFactor = 0.5;

    private int x, y, width, height;
    public Color color;

    public RectWithRects parent = null;

    public int parentIndex = -1;
    private double opacity = 1.0;

    public Color borderColor = new Color(0, 0, 0);

    public int borderSize = 1;

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setColor(int r, int g, int b) {
        color = new Color(r, g, b);
    }

    public void setHeight(int height) {
        this.height = height;
    }



    public void setTransparent() {
        setOpacity(transparencyFactor);
    }

    public void setOpace() {
        setOpacity(1.0);
    }

    public Rect(int width, int height, Color color, ClassType type) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.color = color;
        this.clazz = type;
    }

    public void draw(Graphics g) {
        draw(g, opacity);
    };

    public abstract void draw(Graphics g, double a);

    public boolean contains(Point p) {
        return (p.x >= x && p.x <= x + getWidth() && p.y >= y && p.y <= y + getHeight());
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract Rect clone();

    public abstract void addTo(JPanel p);

    public abstract void removeFrom(JPanel p);

    public abstract Pair<Boolean, Boolean> onHover(Point p);

    public abstract void onMouseReleased();

    public abstract void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e);

}

