package test.rects.multi;

import test.DragDropRectanglesWithSplitPane;
import test.rects.Rect;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;

public abstract class RectWithRects extends Rect {

    public static int spacing = 5;

    public static int emptyRowSize = 25;

    public static int fontSize = 21;

    public static float fontSizeMultiplier = 1.5F;

    public static float fontOffsetMultiplier = 0.5F;

    private int hoveringIndex = -1;

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
            if (i == hoveringIndex) {
                maxWidth = Math.max(maxWidth, DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.getWidth());
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
            if (i == hoveringIndex) {
                heightAcc += DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.getHeight() + spacing;
            }
            else {
                if (r != null) {
                    heightAcc += r.getHeight() + spacing;
                }
                else {
                    heightAcc += emptyRowSize + spacing;
                }
            }

            if (!name.isEmpty()) {
                heightAcc += (int) (fontSize * fontSizeMultiplier);
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
    public void draw(Graphics g, double a) {
        draw(g, a, 1);
    }

    public void draw(Graphics g, double a, int depth) {
        if(g instanceof Graphics2D)
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(getFont());
        }
        context = ((Graphics2D)g).getFontRenderContext();

        var g2 = (Graphics2D) g;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());
        int offset = realHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (r != null) {
                offset = drawSubRect(g, r, name, offset, i, a, depth + 1);
            }
            else {
                offset = drawEmptyBox(g, name, offset, i, a, depth);
            }
        }
    }

    private int drawEmptyBox(Graphics g, String name, int offset, int index, double a, int depth) {
        if (index != hoveringIndex) {
            if (!name.isEmpty()) {
                offset += (int) (fontSize * fontSizeMultiplier);
            }

            g.setColor(new Color(emptyRectsColor.getRed(), emptyRectsColor.getGreen(), emptyRectsColor.getBlue(), (int)(255 * a)));
            g.fillRect(getX() + spacing, getY() + offset, getWidth() - spacing * 2, emptyRowSize);

            g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(255 * a)));
            if (!name.isEmpty()) {
                g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
            }
            return offset + emptyRowSize + spacing;
        }
        else {
            return drawSubRect(g, DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect, name, offset, index, 0.5, depth + 1);
        }
    }

    private int drawSubRect(Graphics g, Rect r, String name, int offset, int index, double a, int depth) {
        if (!name.isEmpty()) {
            offset += (int) (fontSize * fontSizeMultiplier);
        }
        r.setPosition(getX() + spacing, getY() + offset);
        if (!(r instanceof RectWithRects)) {
            r.setWidth(getWidth() - spacing * 2);
        }
        g.setColor(new Color(emptyRectsColor.getRed(), emptyRectsColor.getGreen(), emptyRectsColor.getBlue(), 255));
        g.fillRect(getX() + spacing, getY() + offset, r.getWidth(), r.getHeight());

        r.draw(g, a);

        if (depth == 2) {
            g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 255));
        }
        else {
            g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(255 * a)));
        }
        if (!name.isEmpty()) {
            g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
        }
        return offset + r.getHeight() + spacing;
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
                    heightAcc += (int) (fontSize * fontSizeMultiplier);;
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

    public Rect getSubRect(Point p) {
        if (contains(p) && p.x >= getX() + spacing && p.x <= getX() + getWidth() - spacing) {
            int heightAcc = realHeight();
            for (int i = 0; i < subRects.length && getY() + heightAcc <= p.y; i++) {
                Rect r = subRects[i];
                String name = names[i];
                if (!name.isEmpty()) {
                    heightAcc += (int) (fontSize * fontSizeMultiplier);
                }
                if (r != null && r.contains(p)) {
                    if (r instanceof RectWithRects) {
                        return ((RectWithRects) r).getSubRect(p);
                    }
                    heightAcc += r.getHeight() + spacing;
                } else {
                    heightAcc += emptyRowSize + spacing;
                }
            }
        }
        return this;
    }

    public void setIndex(int i, Rect r) {
        if (r == null) {
            System.out.println("Error trying to add null rect");
        }
        else {
            subRects[i] = r;
        }
    }

    @Override
    public void onHover(Point p) {
        int heightAcc = realHeight();
        hoveringIndex = -1;
        for (int i = 0; i < subRects.length && getY() + heightAcc <= p.y; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (!name.isEmpty()) {
                heightAcc += (int) (fontSize * fontSizeMultiplier);;
            }
            if (r != null) {
                heightAcc += r.getHeight() + spacing;
            }
            else {
                if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                    hoveringIndex = i;
                    break;
                }
                heightAcc += emptyRowSize + spacing;
            }
        }
    };

    @Override
    public void onMouseReleased() {
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.onMouseReleased();
            }
        }
        hoveringIndex = -1;
    };



}
