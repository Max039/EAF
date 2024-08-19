package test.rects.multi;

import test.action.ArrayElementAddedAction;
import test.action.ArrayElementRemovedAction;
import test.models.ClassType;
import test.models.FieldType;
import test.Main;
import test.input.InputHandler;
import test.models.Pair;
import test.rects.RectFactory;
import test.rects.OptionsFieldRect;
import test.rects.Rect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ArrayRect <T extends Rect> extends RectWithRects {

    Color removeButtonColor = new Color(230, 70, 70);

    Color addButtonColor = new Color(220, 220, 220);

    Color buttonBorder = new Color(0, 0, 0);

    private static int buttonBorderSize = 1;

    boolean fillOnCreation = true;

    int buttonWidth = 12;

    enum ButtonType {
        add,
        remove
    }

    public FieldType fillType;

    public ArrayRect(int width, int height, Color color, ClassType type, FieldType field, int num, boolean fillOnCreation) {
        super(width, height, color, type);
        this.fillOnCreation = fillOnCreation;
        this.fillType = field;
        setNamesAndTypes(getEmptyNames(num), generateStringArray(field, num));
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

    public ArrayRect(int width, int height, Color color, ClassType type, FieldType field, String[] names, Rect[] rects, FieldType[] types, boolean fillOnCreation, boolean locked) {
        super(width, height, color, type, names, rects, types, locked);
        this.fillOnCreation = fillOnCreation;
        this.fillType = field;
        if (this.fillOnCreation) {
            fillIfNecessary();
        }
    }

    @Override
    public int getWidth() {
        if (InputHandler.showButtons) {
            return super.getWidth() + buttonWidth + spacing;
        }
        else {
            return super.getWidth();
        }
    }

    public String[] getEmptyNames(int num) {
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            l.add("");
        }
        return l.toArray(new String[l.size()]);
    }

    public static FieldType[] generateStringArray(FieldType input, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }

        FieldType[] resultArray = new FieldType[size];

        Arrays.fill(resultArray, input);

        return resultArray;
    }

    @Override
    public Rect clone() {
        return new ArrayRect<T>(realWidth(), realHeight(), color, getClazz(), fillType, names, subRects, types, fillOnCreation, locked);
    }


    @Override
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, height, removeButtonColor, a, ButtonType.remove);

    };

    @Override
    public void drawOnTop(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public int extraSpacingToRight() {
        if (InputHandler.showButtons) {
            return spacing + buttonWidth;
        }
        else {
            return 0;
        }
    }

    @Override
    public void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a) {
        drawButtonAt(g, x - spacing - buttonWidth, y, emptyRowSize, addButtonColor, a, ButtonType.add);
    };

    @Override
    public int extraSpacingBelow() {
        if (InputHandler.showButtons) {
            return spacing + emptyRowSize;
        }
        else {
            return 0;
        }
    }

    private void drawButtonAt(Graphics g, int x, int y, int height, Color c, int a, ButtonType type) {
        if (InputHandler.showButtons) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
            g.fillRect(x, y, buttonWidth, height);

            g.setColor(new Color(buttonBorder.getRed(), buttonBorder.getGreen(), buttonBorder.getBlue(), a));
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(buttonBorderSize));
            g2.drawRect(x, y, buttonWidth, height);
        }

        /**
        if (type == ButtonType.remove) {
            g2.drawLine(x, y, x + buttonWidth, y + height);
            g2.drawLine(x + buttonWidth, y, x, y + height);
        }
        else {
            g2.drawLine(x + buttonWidth/2, y, x + buttonWidth/2, y + height);
            g2.drawLine(x, y + height/2, x  + buttonWidth, y + height/2);
        }
         **/
    }

    @Override
    public String toString(int level) {
        String res = "";
        res += "[";
        boolean test = Arrays.stream(subRects).anyMatch(Objects::nonNull);
        if (test) {
            res += "\n";
        }

        Rect[] subRects2 = Arrays.stream(subRects).filter(Objects::nonNull).filter(t -> !(t instanceof OptionsFieldRect) || !((String) (((OptionsFieldRect) (t)).comboBox.getSelectedItem())).isEmpty()).toArray(Rect[]::new);
        for (int i = 0; i < subRects2.length; i++) {
            var res2 = repeatString(stringPadding, level + 1 ) + subRects2[i].toString(level + 1);
            res += res2;
            if (i < subRects2.length - 1) {
                res += ",\n";
            }
            else {
                res += "\n";
            }
        }
        if (test) {
            res += repeatString(stringPadding, level);
        }
        res += "]";
        return res;
    }

    public Rect removeElement(int index) {
        // Check if the index is valid
        if (index < 0 || index >= subRects.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        var r = subRects[index];
        Rect[] newRects = new Rect[subRects.length - 1];
        String[] newNames = new String[subRects.length - 1];
        FieldType[] newTypes = new FieldType[subRects.length - 1];

        // Copy elements from the original array to the new array
        for (int i = 0, j = 0; i < subRects.length; i++) {
            if (i != index) {
                newRects[j] = subRects[i];
                newNames[j] = names[i];
                newTypes[j++] = types[i];
            }
            else {
                if (subRects[i] != null) {
                    subRects[i].removeFrom(Main.mainPanel.leftPanel.drawingPanel);
                }
            }
        }

        subRects =  newRects;
        names =  newNames;
        types =  newTypes;
        return r;
    }

    public void addElement(Rect r, int index) {
        Rect[] newRects = new Rect[subRects.length + 1];
        String[] newNames = new String[subRects.length + 1];
        FieldType[] newTypes = new FieldType[subRects.length + 1];

        for (int i = 0, j = 0; i < newRects.length; i++) {
            if (i != index) {
                newRects[i] = subRects[j];
                newNames[i] = names[j];
                newTypes[i] = types[j++];
            }
            else {
                newNames[i] = "";
                newTypes[i] = fillType;
                newRects[i] = r;
            }
        }


        subRects =  newRects;
        names =  newNames;
        types =  newTypes;



        if (r != null) {
            r.addTo(Main.mainPanel.leftPanel.drawingPanel);
        }
    }

    public Pair<Boolean, Integer> getArrayButton(Point p) {
        int heightAcc = realHeight();
        if (p.x >= getX() + getWidth() - spacing - buttonWidth && p.x <= getX() + getWidth() - spacing) {
            for (int i = 0; i < subRects.length + 1 && getY() + heightAcc <= p.y; i++) {
                if (i < subRects.length) {
                    Rect r = subRects[i];
                    if (r != null) {
                        if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + r.getHeight()) {
                            return new Pair<Boolean, Integer>(true, i);
                        }
                        heightAcc += r.getHeight() + spacing * 2;
                    }
                    else {
                        if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                            return new Pair<Boolean, Integer>(true, i);
                        }
                        heightAcc += emptyRowSize + spacing * 2;
                    }
                }
                else {
                    if (p.y >= getY() + heightAcc && p.y <= getY() + heightAcc + emptyRowSize) {
                        return new Pair<Boolean, Integer>(true, -1);
                    }
                }
            }
        }
        return new Pair<Boolean, Integer>(false, -1);
    };

    public boolean pressedButton(Point p) {
        var res = getArrayButton(p);
        if (res.getFirst()) {
            if (res.getSecond() == -1) {
                Rect r;
                if (fillOnCreation) {
                    r = RectFactory.getRectFromFieldType(fillType, null);
                }
                else {
                    r = null;
                }
                addElement(r, subRects.length);
                InputHandler.actionHandler.action(new ArrayElementAddedAction(this, r));
            }
            else {
                var r = removeElement(res.getSecond());
                InputHandler.actionHandler.action(new ArrayElementRemovedAction(this, r, res.getSecond()));
            }

        }
        Main.mainPanel.leftPanel.revalidate();
        Main.mainPanel.leftPanel.repaint();
        return res.getFirst();
    }


    public void fillIfNecessary() {
        for (int i = 0; i < types.length; i++) {
            var r = subRects[i];
            if (r == null) {
                var c = types[i];
                setIndex(i, RectFactory.getRectFromFieldType(c, null));
            }
        }
    }

    public Rect removeLast() {
        return removeElement(Math.max(subRects.length-1, 0));
    }

    public void addLast(Rect r) {
        addElement(r, Math.max(subRects.length-1, 0));
    }


}
