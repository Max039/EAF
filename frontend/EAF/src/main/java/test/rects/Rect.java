package test.rects;

import compiler.ClassType;
import org.json.JSONObject;
import test.Main;
import test.InputHandler;
import test.Pair;
import test.RectPanel;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class Rect {

    public boolean selected = false;

    public void select() {
        selected = true;
    }
    public void unselect() {
        selected = false;
    }
    public static Color selectedRectColor = new Color(RectPanel.instanceColor.getRed() + 20, RectPanel.instanceColor.getGreen() + 20, RectPanel.instanceColor.getBlue() + 20);

    public ClassType getClazz() {
        return clazz;
    }

    public ClassType clazz;

    public static double transparencyFactor = 0.5;

    private int x, y, width, height;
    public Color color;

    public RectWithRects parent = null;

    public int parentIndex = -1;
    private double opacity = 1.0;

    public Color borderColor = new Color(85, 85, 85);

    public int borderSize = 1;

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

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

    public boolean valid = true;

    public void setTransparent() {
        setOpacity(transparencyFactor);
    }

    public void setOpace() {
        setOpacity(1.0);
    }

    public Rect(int width, int height, Color color, ClassType type) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.color = color;
        this.clazz = type;
    }

    public void draw(Graphics g) {
        draw(g, opacity);
    };

    public abstract void draw(Graphics g, double a);

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

    public abstract Pair<Boolean, Boolean> onHover(Point p);

    public abstract void onMouseReleased();

    public abstract void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e);

    public abstract void setValidity();

    public abstract void ifInvalid();

    public abstract JSONObject toJson();

    public abstract String toString(int level);

    public static String repeatString(String str, int times) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < times; i++) {
            result.append(str);
        }

        return result.toString();
    }

    public static String stringPadding = "  ";

    public void registerString(String s, int y) {
        if (!Main.subFrame.leftPanelTextField.getText().isEmpty()) {
            if (Main.subFrame.leftPanel.hasRect(this)) {
                var res = InputHandler.stringMarker.get(s);
                if (res != null && !res.contains(y)) {
                    res.add(y);
                } else {
                    var ar = new ArrayList<Integer>();
                    ar.add(y);
                    InputHandler.stringMarker.put(s, ar);
                }

            }
        }
    }
    }

