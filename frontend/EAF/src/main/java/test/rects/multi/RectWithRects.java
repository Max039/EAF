package test.rects.multi;

import action.AddedRectAction;
import action.DeletedRectAction;
import action.RemovedDataAction;
import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import compiler.SyntaxTree;
import org.json.JSONArray;
import org.json.JSONObject;
import test.DragDropRectanglesWithSplitPane;
import test.Pair;
import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.TextFieldRect;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    Color arrayColor = new Color(104, 151, 187);

    FontRenderContext context = null;

    public Rect[] getSubRects() {
        return subRects;
    }

    Rect[] subRects = new Rect[0];
    String[] names = new String[0];

    FieldType[] types = new FieldType[0];


    public abstract int extraSpacingToRight();

    public abstract int extraSpacingBelow();

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    boolean locked = false;


    public RectWithRects(int width, int height, Color color, ClassType type, String[] names, FieldType[] types) {
        this(width, height, color, type);
        setNamesAndTypes(names, types);
    }

    public RectWithRects(int width, int height, Color color, ClassType type, String[] names, Rect[] subRects, FieldType[] types, boolean locked) {
        this(width, height, color, type, names, types);
        this.locked = locked;
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
                setIndex(i, r.clone());
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
                if (r instanceof TextFieldRect || r instanceof OptionsFieldRect) {
                    if (r instanceof TextFieldRect) {
                        maxWidth = Math.max(maxWidth, ((TextFieldRect) r).getTextWidth() - extraSpacingToRight());
                    }
                    else {
                        maxWidth = Math.max(maxWidth, ((OptionsFieldRect) r).getWidth() - extraSpacingToRight());
                    }
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


    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        int offset = realHeight();
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.setPosition(getX() + spacing, getY() + offset);
                offset += r.getHeight() + spacing * 2;
            }
            else {
                boolean typeIndexMatch = indexDoesNotMatchesDragged(i);
                if (i != hoveringIndex || typeIndexMatch) {
                    offset += emptyRowSize + spacing * 2;
                }
                else {
                    DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.setPosition(getX() + spacing, getY() + offset);
                    offset += DragDropRectanglesWithSplitPane.subFrame.leftPanel.draggingRect.getHeight() + spacing * 2;
                }

            }
        }
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


            if (!name.isEmpty()) {
                if (types[index].arrayCount > 0) {
                    g.setColor(new Color(arrayColor.getRed(), arrayColor.getGreen(), arrayColor.getBlue(), (int)(255 * a)));
                }
                else {
                    g.setColor(new Color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), (int)(255 * a)));
                }
                g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
                registerString(name, getY() + offset - (int)(fontSize * fontOffsetMultiplier) - fontSize);
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

        if (types[index].arrayCount > 0) {
            g.setColor(new Color(arrayColor.getRed(), arrayColor.getGreen(), arrayColor.getBlue(), (int)(255 * a)));
        }
        else {
            g.setColor(new Color(fieldColor.getRed(), fieldColor.getGreen(), fieldColor.getBlue(), (int)(255 * a)));
        }
        if (!name.isEmpty()) {
            g.drawString(name, getX() + spacing, getY() + offset - (int)(fontSize * fontOffsetMultiplier));
            registerString(name, getY() + offset - (int)(fontSize * fontOffsetMultiplier) - fontSize);
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
        DragDropRectanglesWithSplitPane.subFrame.erroRects.remove(this);
        for (int i = 0; i < subRects.length; i++) {
            Rect r = subRects[i];
            if (r != null) {
                r.removeFrom(p);
            }
        }
    }

    public Pair<Boolean, Pair<Rect, Integer>> setIndex(Point p, Rect rec) {
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
                        var res = ((RectWithRects)r).setIndex(p, rec);
                        if (res.getFirst()) {
                            return res;
                        }
                    }
                    heightAcc += r.getHeight() + spacing * 2;
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize && !indexDoesNotMatchesDragged(i)) {
                        setIndex(i, rec);
                        return new Pair<>(true, new Pair<>(this, i));
                    }
                    heightAcc += emptyRowSize + spacing * 2;
                }
            }
        }
        return new Pair<>(false, new Pair<>(this, -1));
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
        if (r != null) {
            r.parent = this;
            r.parentIndex = i;
        }
        subRects[i] = r;

        DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
        DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
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

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {
        var res = getIndex(p);

        if (res.getFirst() && !left) {
            var index = types[res.getSecond()];
            if (!index.primitive) {
                var clazz = SyntaxTree.classRegister.get(index.typeName);
                var valid = clazz.getAllClassTypes();


                valid = valid.stream()
                        .sorted(Comparator.comparing(classType -> classType.name))
                        .filter(t -> !t.isAbstract)
                        .collect(Collectors.toList());

                // Create the popup menu for the first 5 options
                JPopupMenu popupMenu = new JPopupMenu();
                int maxVisibleItems = 5;
                for (int i = 0; i < Math.min(valid.size(), maxVisibleItems); i++) {
                    var item = valid.get(i);
                    JMenuItem menuItem = new JMenuItem(item.name);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            var rect = DragDropRectanglesWithSplitPane.getRectFromClassType(item);
                            rect.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
                            setIndex(res.getSecond(), rect);


                            DragDropRectanglesWithSplitPane.actionHandler.action(new AddedRectAction(RectWithRects.this, rect, res.getSecond()));
                        }
                    });
                    popupMenu.add(menuItem);
                }

                // Add "Show More" option if there are more than 5 options
                if (valid.size() > maxVisibleItems) {
                    JMenuItem showMoreItem = new JMenuItem("Show More...");
                    showMoreItem.setFont(showMoreItem.getFont().deriveFont(Font.BOLD | Font.ITALIC)); // Make text bold and italic
                    List<ClassType> finalValid = valid;
                    showMoreItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Create a new scrollable window with all options
                            JDialog showMoreDialog = new JDialog(DragDropRectanglesWithSplitPane.mainFrame, "All Options", true);
                            showMoreDialog.setSize(300, 400);
                            showMoreDialog.setLocationRelativeTo(DragDropRectanglesWithSplitPane.mainFrame);

                            JPanel mainPanel = new JPanel();
                            mainPanel.setLayout(new BorderLayout());

                            // Create a panel with BorderLayout for the label and search bar
                            JPanel searchPanel = new JPanel(new BorderLayout());

                            JLabel filterLabel = new JLabel("Filter:");
                            searchPanel.add(filterLabel, BorderLayout.WEST);

                            JTextField searchField = new JTextField();
                            searchPanel.add(searchField, BorderLayout.CENTER);

                            mainPanel.add(searchPanel, BorderLayout.NORTH);

                            JPanel listPanel = new JPanel();
                            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                            JScrollPane scrollPane = new JScrollPane(listPanel);
                            mainPanel.add(scrollPane, BorderLayout.CENTER);

                            // Method to update the list based on the search
                            Runnable updateList = new Runnable() {
                                @Override
                                public void run() {
                                    String searchText = searchField.getText().toLowerCase();
                                    listPanel.removeAll();

                                    for (var fullItem : finalValid) {
                                        if (fullItem.name.toLowerCase().contains(searchText)) {
                                            JButton button = new JButton(fullItem.name);
                                            button.setHorizontalAlignment(SwingConstants.CENTER);
                                            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Ensure full width and fixed height
                                            button.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    var rect = DragDropRectanglesWithSplitPane.getRectFromClassType(fullItem);
                                                    rect.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
                                                    setIndex(res.getSecond(), rect);


                                                    DragDropRectanglesWithSplitPane.actionHandler.action(new AddedRectAction(RectWithRects.this, rect, res.getSecond()));
                                                    showMoreDialog.dispose(); // Close the dialog after selection
                                                }
                                            });
                                            listPanel.add(button);
                                        }
                                    }

                                    listPanel.revalidate();
                                    listPanel.repaint();
                                }
                            };

                            // Add a listener to the search field to update the list as the user types
                            searchField.getDocument().addDocumentListener(new DocumentListener() {
                                @Override
                                public void insertUpdate(DocumentEvent e) {
                                    updateList.run();
                                }

                                @Override
                                public void removeUpdate(DocumentEvent e) {
                                    updateList.run();
                                }

                                @Override
                                public void changedUpdate(DocumentEvent e) {
                                    updateList.run();
                                }
                            });

                            // Initial population of the list
                            updateList.run();

                            showMoreDialog.add(mainPanel);
                            showMoreDialog.setVisible(true);
                        }
                    });
                    popupMenu.add(showMoreItem);

                }

                popupMenu.show(DragDropRectanglesWithSplitPane.mainFrame, p2.x, p2.y);
            }

        } else if (this instanceof ClassRect) {
            if (left && parent != null && !locked) {
                // Copy, set dragging, delete, etc.
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(RectWithRects.this);
                var s = parent;
                parent.subRects[parentIndex] = null;
                parent = null;
                DragDropRectanglesWithSplitPane.subFrame.setDraggingRect(RectWithRects.this, e, new Point(e.getPoint().x - getX(), e.getPoint().y - getY()), s);
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
                DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
            } else if (!left) {
                JPopupMenu popupMenu = new JPopupMenu();

                Point p3 = new Point();
                p3.x = p2.x + DragDropRectanglesWithSplitPane.mainFrame.getX();
                p3.y = p2.y + DragDropRectanglesWithSplitPane.mainFrame.getY();
                JMenuItem info = new JMenuItem("Info");
                info.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        ClassType.displayClassInfo(clazz, p3);
                    }
                });
                popupMenu.add(info);


                if (!locked) {
                    JMenuItem menuItem = new JMenuItem("Delete");
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (parent != null) {
                                parent.subRects[parentIndex] = null;
                            }

                            DragDropRectanglesWithSplitPane.actionHandler.action(new DeletedRectAction(parent, RectWithRects.this, parentIndex));
                            DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(RectWithRects.this);
                            DragDropRectanglesWithSplitPane.subFrame.leftPanel.revalidate();
                            DragDropRectanglesWithSplitPane.subFrame.leftPanel.repaint();
                        }
                    });
                    popupMenu.add(menuItem);
                }

                popupMenu.show(DragDropRectanglesWithSplitPane.mainFrame, p2.x, p2.y);
            }
        }
        else if (this instanceof ArrayRect) {
            if (left && DragDropRectanglesWithSplitPane.showButtons) {
                ((ArrayRect)this).pressedButton(p);
            }
        }
    }


    @Override
    public void setValidity() {
        DragDropRectanglesWithSplitPane.subFrame.erroRects.remove(this);
        valid = true;
    };

    @Override
    public void ifInvalid() {

    };


    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        if (this instanceof ArrayRect) {
            o.put("type", "array");
            o.put("primitive", ((ArrayRect)this).fillType.primitive);
            o.put("count", ((ArrayRect)this).fillType.arrayCount);
        }
        if (this instanceof ClassRect) {
            o.put("type", "instance");
        }
        o.put("sub-type", clazz.name);
        o.put("package", clazz.pack);

        JSONArray a = new JSONArray();
        for (int i = 0; i < subRects.length; i++) {

            var r = subRects[i];

            if (r != null && (!(r instanceof ArrayRect) || clazz.fields.values().stream().toList().get(i).getSecond() == null)) {
                if (!(r instanceof OptionsFieldRect) || !((String) (((OptionsFieldRect) (r)).comboBox.getSelectedItem())).isEmpty()) {
                    JSONObject o2 = r.toJson();
                    o2.put("field-name", names[i]);
                    a.put(o2);
                }
            }
        }
        o.put("value", a);
        return  o;
    }

}
