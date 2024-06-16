package test;

import javax.swing.*;
import java.awt.*;

class RectWithColor extends Rect {
    public RectWithColor(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    @Override
    void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    @Override
    public Rect clone() {
        return new RectWithColor(x, y, width, height, color);
    }

    @Override
    public void addTo(JPanel p) {

    }

    @Override
    public void removeFrom(JPanel p) {

    }

}


