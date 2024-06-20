package test;

import javax.swing.*;
import java.awt.*;

public class RectWithRects extends Rect {

    public static int spacing = 5;

    public static int emptyRowSize = 12;

    Rect[] subRects;
    String[] names;
    public RectWithRects(int width, int height, Color color, String[] names) {
        super(width, height, color);
        this.names = names.clone();
        this.subRects = new Rect[names.length];
        for (int i = 0; i < names.length; i++) {
            subRects[i] = null;
        }
    }

    public RectWithRects(int width, int height, Color color, String[] names, Rect[] subRects) {
        this(width, height, color, names);
        this.subRects = subRects.clone();
    }

    @Override
    public int getWidth() {
        int maxWidth = super.getWidth();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                maxWidth = Math.max(maxWidth, r.getWidth());
            }
        }
        return spacing * 2 + maxWidth;
    }

    @Override
    public int getHeight() {
        int heightAcc = super.getHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                heightAcc += r.getHeight() + spacing;
            }
            else {
                heightAcc += emptyRowSize + spacing;
            }
        }
        return heightAcc;
    }

    @Override
    void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        int offset = super.getHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.setPosition(getX() + spacing, getY() + offset);
                if (!(r instanceof RectWithRects)) {
                    r.setWidth(getWidth() - spacing * 2);
                }
                r.draw(g);
                offset += r.getHeight() + spacing;
            }
            else {
                g.setColor(Color.white);
                g.fillRect(getX() + spacing, getY() + offset, getWidth() - spacing * 2, emptyRowSize);
                offset += emptyRowSize + spacing;
            }
        }
    }

    @Override
    public Rect clone() {
        return new RectWithRects(super.getWidth(), super.getHeight(), color, names, subRects);
    }

    @Override
    public void addTo(JPanel p) {
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.addTo(p);
            }
        }
    }

    @Override
    public void removeFrom(JPanel p) {
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.removeFrom(p);
            }
        }
    }

    public boolean setIndex(Point p, Rect rec) {
        if (contains(p) && p.x >= getX() + spacing && p.x <= getX() + getWidth() - spacing) {
            int heightAcc = super.getHeight();
            for (int i = 0; i < subRects.length && getY() + heightAcc <= p.y; i++) {
                Rect r = subRects[i];
                if (r != null) {
                    if (r instanceof RectWithRects) {
                        boolean subRecursion = ((RectWithRects)r).setIndex(p, rec);
                        if (subRecursion) {
                            return true;
                        }
                    }
                    heightAcc += r.getHeight() + spacing;
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                        setIndex(i, rec);
                        return true;
                    }
                    heightAcc += emptyRowSize + spacing;
                }
            }
        }
        return false;
    }

    public void setIndex(int i, Rect r) {
        if (r == null) {
            System.out.println("Error trying to add null rect");
        }
        else {
            subRects[i] = r;
        }
    }

}
