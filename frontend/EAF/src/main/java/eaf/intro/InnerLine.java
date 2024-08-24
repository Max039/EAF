package eaf.intro;

import eaf.Main;

import java.awt.*;

public class InnerLine {


    public Graphics2D g2d;
    public Color c;
    public int y;
    public int upperY;
    public int lowerY;
    public int a;
    public InnerLine(Graphics2D g2d, Color c, int y, int upperY, int lowerY, int a) {
        this.g2d = g2d;
        this.c = c;
        this.y = y;
        this.upperY = upperY;
        this.lowerY = lowerY;
        this.a = a;
    }

    public void draw(int x1, int x2) {
        DoubleHelixAnimation.drawLineInRange(g2d, c, x1, y, x2, y, upperY, lowerY, a);
    }

}
