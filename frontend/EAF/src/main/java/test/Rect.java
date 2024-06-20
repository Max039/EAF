package test;

import javax.swing.*;
import java.awt.*;

public abstract class Rect {
    private int x, y, width, height;
    Color color;

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

    public Rect(int width, int height, Color color) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    abstract void draw(Graphics g);

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

}

