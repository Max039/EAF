package test.rects.multi;

import compiler.ClassType;
import compiler.FieldType;
import compiler.SyntaxTree;
import test.DragDropRectanglesWithSplitPane;
import test.Pair;
import test.rects.Rect;
import test.rects.RectWithColorAndTextBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

import static compiler.FieldValue.doesTypesMatch;

public abstract class RectWithRects extends Rect {

    public static int spacing = 5;

    public static int emptyRowSize = 25;

    public static int fontSize = 21;


    public static float fontOffsetMultiplier = 0.5F;



    private int hoveringIndex = -1;


    Color fieldColor = new Color(213, 234, 238);

    Color nameColor = new Color(203, 116, 47);

    Color emptyRectsColor = new Color(255, 255, 255);

    Color invalidRectsColor = new Color(190, 70, 70);


    FontRenderContext context = null;

    Rect[] subRects = new Rect[0];
    String[] names = new String[0];

    FieldType[] types = new FieldType[0];


    public abstract int extraSpacingToRight();

    public abstract int extraSpacingBelow();


    public RectWithRects(ClassType type) {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255), type);
    }

    public RectWithRects(int width, int height, Color color, ClassType type, String[] names, FieldType[] types) {
        this(width, height, color, type);
        setNamesAndTypes(names, types);
    }

    public RectWithRects(int width, int height, Color color, ClassType type, String[] names, Rect[] subRects, FieldType[] types) {
        this(width, height, color, type, names, types);
        setRects(subRects);
    }

    public RectWithRects(int width, int height, Color color, ClassType type) {
        super(width, height, color, type);
    }

    public void setNamesAndTypes(String[] names, FieldType[] types) {
        this.names = names.clone();
        this.subRects = new Rect[names.length];
        this.types = types;
        for (int i = 0; i < names.length; i++) {
            subRects[i] = null;
        }
    }

    public void setRects(Rect[] subRects) {
        this.subRects = new Rect[subRects.length];
        for (int i = 0; i < subRects.length; i++) {
            var r = subRects[i];
            if (r != null) {
                this.subRects[i] = r.clone();
            }
        }
    }



    public abstract void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a);

    public abstract void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a);

    public abstract void drawOnTop(Graphics g, int x, int y, int width, int height, int a);

    @Override
    public int getWidth() {
        int maxWidth = realWidth();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (r != null) {
                if (r instanceof RectWithColorAndTextBox) {
                    maxWidth = Math.max(maxWidth, ((RectWithColorAndTextBox) r).getTextWidth() - extraSpacingToRight());
                }
                else {
                    maxWidth = Math.max(maxWidth, r.getWidth());
                }
            }
            if (i == hoveringIndex && !indexDoesNotMatchesDragged(i)) {
                maxWidth = Math.max(maxWidth, DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.getWidth());
            }
            if (context != null) {
                maxWidth = Math.max(maxWidth, (int) getFont().getStringBounds(name, context).getWidth());
            }
        }
        if (context != null && this instanceof ClassRect) {
            maxWidth = Math.max(maxWidth, (int) getFont().getStringBounds(clazz.name, context).getWidth());
        }
        return spacing * 2 + maxWidth + extraSpacingToRight();
    }

    @Override
    public int getHeight() {
        int heightAcc = realHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            String name = names[i];
            if (i == hoveringIndex && !indexDoesNotMatchesDragged(i)) {
                heightAcc += DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.getHeight() + spacing;
            }
            else {
                if (r != null) {
                    heightAcc += r.getHeight() + spacing * 2;
                }
                else {
                    heightAcc += emptyRowSize + spacing * 2;
                }
            }

            if (!name.isEmpty()) {
                heightAcc += (int) (fontSize);
            }
        }
        return heightAcc + extraSpacingBelow();
    }

    public int realHeight() {
        return super.getHeight();
    }

    public int realWidth() {
        return super.getWidth();
    }

    public static Font getFont() {
        return new Font("TimesRoman", Font.PLAIN, (int)(fontSize));
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
        g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX() + borderSize, getY() + borderSize, getWidth() - borderSize * 2, getHeight() - borderSize * 2);
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
        drawOnTop(g, getX(), getY(), getWidth(), getHeight(), (int)(255 * a));
        drawOnTopBelow(g, getX() + getWidth(), getY() + getHeight() - extraSpacingBelow(), getWidth(), getHeight() - extraSpacingBelow(), (int)(255 * a));
    }

    private boolean indexDoesNotMatchesDragged(int index) {
        var type = types[index];

        if (DragDropRectanglesWithSplitPane.subFrame.draggedRect != null) {
            var clazz = DragDropRectanglesWithSplitPane.subFrame.draggedRect.getClazz();
            boolean typeCheck;
            if (type.primitive) {
                typeCheck = type.typeName.equals(clazz.name);
            } else {
                typeCheck = doesTypesMatch(type, clazz);
            }

            return !typeCheck;
        }
        return false;
    }

    private int drawEmptyBox(Graphics g, String name, int offset, int index, double a, int depth) {
        boolean typeIndexMatch = indexDoesNotMatchesDragged(index);
        if (index != hoveringIndex || typeIndexMatch) {
            if (!name.isEmpty()) {
                offset += (int) (fontSize);
            }

            if (typeIndexMatch && index == hoveringIndex) {
                g.setColor(new Color(invalidRectsColor.getRed(), invalidRectsColor.getGreen(), invalidRectsColor.getBlue(), (int)(255 * a)));
            }
            else {
                g.setColor(new Color(emptyRectsColor.getRed(), emptyRectsColor.getGreen(), emptyRectsColor.getBlue(), (int)(255 * a)));
            }
            g.fillRect(getX() + spacing, getY() + offset, getWidth() - spacing * 2 - extraSpacingToRight(), emptyRowSize);

            g.setColor(new Color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), (int)(255 * a)));
            if (!name.isEmpty()) {
                g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
            }
            drawOnTopForEachRow(g, getX() + getWidth(), getY() + offset, getWidth() - spacing * 2, emptyRowSize, (int)(255 * a));
            return offset + emptyRowSize + spacing * 2;
        }
        else {
            return drawSubRect(g, DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect, name, offset, index, transparencyFactor, depth + 1);

        }
    }

    private int drawSubRect(Graphics g, Rect r, String name, int offset, int index, double a, int depth) {
        if (!name.isEmpty()) {
            offset += (int) (fontSize);
        }
        r.setPosition(getX() + spacing, getY() + offset);
        if (!(r instanceof RectWithRects)) {
            r.setWidth(getWidth() - spacing * 2 - extraSpacingToRight());
        }

        r.draw(g, a);

        g.setColor(new Color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), (int)(255 * a)));
        if (!name.isEmpty()) {
            g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
        }
        drawOnTopForEachRow(g, getX() + getWidth(), getY() + offset, r.getWidth(), r.getHeight(), (int)(255 * a));
        return offset + r.getHeight() + spacing * 2;
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
                    heightAcc += (int) (fontSize);;
                }
                if (r != null) {
                    if (r instanceof RectWithRects) {
                        boolean subRecursion = ((RectWithRects)r).setIndex(p, rec);
                        if (subRecursion) {
                            return true;
                        }
                    }
                    heightAcc += r.getHeight() + spacing * 2;
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize && !indexDoesNotMatchesDragged(i)) {
                        setIndex(i, rec);
                        return true;
                    }
                    heightAcc += emptyRowSize + spacing * 2;
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
                    heightAcc += (int) (fontSize);
                }
                if (r != null && r.contains(p)) {
                    if (r instanceof RectWithRects) {
                        return ((RectWithRects) r).getSubRect(p);
                    }
                    heightAcc += r.getHeight() + spacing * 2;
                } else {
                    heightAcc += emptyRowSize + spacing * 2;
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
            r.parent = this;
            r.parentIndex = i;
        }
    }

    @Override
    public Pair<Boolean, Boolean> onHover(Point p) {
        var res = getIndex(p);
        hoveringIndex = -1;
        if (res.getFirst()) {
            hoveringIndex = res.getSecond();
            if (indexDoesNotMatchesDragged(hoveringIndex)) {
                return new Pair<>(true, true);
            }
            else {
                return new Pair<>(false, false);
            }
        }
        else {
            return new Pair<>(true, false);
        }
    };

    public Pair<Boolean, Integer> getIndex(Point p) {
        int heightAcc = realHeight();
        if (p.x >= getX() + spacing && p.x <= getX() + getWidth() - spacing) {
            for (int i = 0; i < subRects.length && getY() + heightAcc <= p.y; i++) {
                Rect r = subRects[i];
                String name = names[i];
                if (!name.isEmpty()) {
                    heightAcc += (int) (fontSize);;
                }
                if (r != null) {
                    heightAcc += r.getHeight() + spacing * 2;
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                        return new Pair<Boolean, Integer>(true, i);
                    }
                    heightAcc += emptyRowSize + spacing * 2;
                }
            }
        }
        return new Pair<Boolean, Integer>(false, -1);
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

    public void fillIfNecessary() {
        for (int i = 0; i < types.length; i++) {
            var r = subRects[i];
            if (r == null) {
                var c = types[i];
                subRects[i] = DragDropRectanglesWithSplitPane.getRectFromFieldType(c, null);
            }
        }
    }

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {
        var res = getIndex(p);

        if (res.getFirst() && !left) {
            var index = types[res.getSecond()];
            if (!index.primitive) {
                var clazz = SyntaxTree.classRegister.get(index.typeName);
                var valid = clazz.getAllClassTypes();

                // Create the popup menu for the first 5 options
                JPopupMenu popupMenu = new JPopupMenu();
                int maxVisibleItems = 5;
                for (int i = 0; i < Math.min(valid.size(), maxVisibleItems); i++) {
                    var item = valid.get(i);
                    JMenuItem menuItem = new JMenuItem(item.name);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setAndRedrawClass(item, res.getSecond());
                        }
                    });
                    popupMenu.add(menuItem);
                }

                // Add "Show More" option if there are more than 5 options
                if (valid.size() > maxVisibleItems) {
                    JMenuItem showMoreItem = new JMenuItem("Show More...");
                    showMoreItem.setFont(showMoreItem.getFont().deriveFont(Font.BOLD | Font.ITALIC)); // Make text bold and italic
                    showMoreItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Create a new scrollable window with all options
                            JDialog showMoreDialog = new JDialog(DragDropRectanglesWithSplitPane.mainFrame, "All Options", true);
                            showMoreDialog.setSize(300, 400);
                            showMoreDialog.setLocationRelativeTo(DragDropRectanglesWithSplitPane.mainFrame);

                            JPanel listPanel = new JPanel();
                            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                            JScrollPane scrollPane = new JScrollPane(listPanel);

                            for (var fullItem : valid) {
                                JButton button = new JButton(fullItem.name);
                                button.setHorizontalAlignment(SwingConstants.CENTER);
                                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Ensure full width and fixed height
                                button.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        setAndRedrawClass(fullItem, res.getSecond());
                                        showMoreDialog.dispose(); // Close the dialog after selection
                                    }
                                });
                                listPanel.add(button);
                            }

                            showMoreDialog.add(scrollPane);
                            showMoreDialog.setVisible(true);
                        }
                    });
                    popupMenu.add(showMoreItem);
                }

                popupMenu.show(DragDropRectanglesWithSplitPane.mainFrame, p2.x, p2.y);
            }

        } else if (this instanceof ClassRect) {
            if (left) {
                // Copy, set dragging, delete, etc.
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(RectWithRects.this);
                if (parent != null) {
                    parent.subRects[parentIndex] = null;
                }
                DragDropRectanglesWithSplitPane.subFrame.setDraggingRect(RectWithRects.this, e, new Point(e.getPoint().x - getX(), e.getPoint().y - getY()));
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
            } else {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem("Delete");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (parent != null) {
                            parent.subRects[parentIndex] = null;
                        }
                        DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(RectWithRects.this);
                        DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
                        DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
                    }
                });
                popupMenu.add(menuItem);
                popupMenu.show(DragDropRectanglesWithSplitPane.mainFrame, p2.x, p2.y);
            }
        }
    }

    public void setAndRedrawClass(ClassType t, int index) {
        var rect = DragDropRectanglesWithSplitPane.getRectFromClassType(t);
        rect.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        setIndex(index, rect);
        DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
        DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
    }

}
