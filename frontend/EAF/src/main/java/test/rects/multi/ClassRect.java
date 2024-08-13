package test.rects.multi;

import compiler.ClassType;
import compiler.FieldType;
import org.json.JSONObject;
import test.rects.Rect;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class ClassRect extends RectWithRects {

    public ClassRect(int width, int height, Color color, ClassType type, String[] names, Rect[] rects, FieldType[] types) {
        super(width, height, color, type, names, rects, types);
    }



    @Override
    public Rect clone() {
        System.out.println("class");
        return new ClassRect(realWidth(), realHeight(), color, getClazz(), names, subRects, types);
    }

    @Override
    public void drawOnTopForEachRow(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public int extraSpacingToRight() {
        return 0;
    }

    @Override
    public void drawOnTopBelow(Graphics g, int x, int y, int width, int height, int a) {

    };

    @Override
    public void drawOnTop(Graphics g, int x, int y, int width, int height, int a) {
        g.setColor(new Color(nameColor.getRed(), nameColor.getGreen(), nameColor.getBlue(), a));
        if (!clazz.name.isEmpty()) {
            g.drawString(clazz.name, x + spacing, y + spacing + fontSize);
            registerString(clazz.name, y + spacing);
        }
    };

    @Override
    public int extraSpacingBelow() {
        return 0;
    }

    @Override
    public String toString(int level) {
        String res = "'" + clazz.name + "' {";
        boolean test = clazz.fields.values().stream().anyMatch(t -> t.getSecond() == null);
        if (test) {
            res += "\n";
        }
        int i = 0;
        for (var t : clazz.fields.entrySet()) {
            if (t.getValue().getSecond() == null) {
                res += repeatString(stringPadding, level + 1 ) + "'" + t.getKey() + "' := " + subRects[i].toString(level + 1);
                res += ";\n";
            }
            i++;
        }
        if (test) {
            res += repeatString(stringPadding, level);
        }
        res += "}";
        return res;
    }

}
