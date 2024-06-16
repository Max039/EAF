package test;

import javax.swing.*;
import java.awt.*;

abstract class Rect {
    int x, y, width, height;
    Color color;

    public Rect(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    abstract void draw(Graphics g);

    public boolean contains(Point p) {
        return (p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract Rect clone();

    public abstract void addTo(JPanel p);

    public abstract void removeFrom(JPanel p);

}

