package test;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;

public abstract class RectWithRects extends Rect {

    public static int spacing = 5;

    public static int emptyRowSize = 12;

    public static int fontSize = 21;

    Color textColor = new Color(255, 255, 255);

    Color emptyRectsColor = new Color(255, 255, 255);

    FontRenderContext context = null;

    Rect[] subRects = new Rect[0];
    String[] names = new String[0];

    public RectWithRects(int width, int height, Color color, String[] names) {
        this(width, height, color);
        setNames(names);
    }

    public void setNames(String[] names) {
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

    public RectWithRects(int width, int height, Color color) {
        super(width, height, color);
    }

    @Override
    public int getWidth() {
        int maxWidth = realWidth();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (r != null) {
                maxWidth = Math.max(maxWidth, r.getWidth());
            }
            if (context != null) {
                maxWidth = Math.max(maxWidth, (int) getFont().getStringBounds(name, context).getWidth());
            }
        }
        return spacing * 2 + maxWidth;
    }

    @Override
    public int getHeight() {
        int heightAcc = realHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (r != null) {
                heightAcc += r.getHeight() + spacing;
            }
            else {
                heightAcc += emptyRowSize + spacing;
            }
            if (!name.isEmpty()) {
                heightAcc += (int) (fontSize * 1.5F);
            }
        }
        return heightAcc;
    }

    public int realHeight() {
        return super.getHeight();
    }

    public int realWidth() {
        return super.getWidth();
    }

    public static Font getFont() {
        return new Font("TimesRoman", Font.PLAIN, fontSize);
    }

    @Override
    void draw(Graphics g) {
        if(g instanceof Graphics2D)
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(getFont());
        }
        context = ((Graphics2D)g).getFontRenderContext();

        g.setColor(color);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        int offset = realHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (r != null) {
                if (!name.isEmpty()) {
                    offset += (int) (fontSize * 1.5F);
                }
                r.setPosition(getX() + spacing, getY() + offset);
                if (!(r instanceof RectWithRects)) {
                    r.setWidth(getWidth() - spacing * 2);
                }
                r.draw(g);

                g.setColor(textColor);
                if (!name.isEmpty()) {
                    g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * 0.5F));
                }
                offset += r.getHeight() + spacing;
            }
            else {
                if (!name.isEmpty()) {
                    offset += (int) (fontSize * 1.5F);
                }

                g.setColor(emptyRectsColor);
                g.fillRect(getX() + spacing, getY() + offset, getWidth() - spacing * 2, emptyRowSize);

                g.setColor(textColor);
                if (!name.isEmpty()) {
                    g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * 0.5F));
                }
                offset += emptyRowSize + spacing;
            }
        }
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
            int heightAcc = realHeight();
            for (int i = 0; i < subRects.length && getY() + heightAcc <= p.y; i++) {
                Rect r = subRects[i];
                String name = names[i];
                if (!name.isEmpty()) {
                    heightAcc += (int) (fontSize * 1.5F);;
                }
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
