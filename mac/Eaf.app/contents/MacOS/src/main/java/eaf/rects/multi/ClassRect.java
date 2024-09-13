package eaf.rects.multi;

import eaf.compiler.SyntaxTree;
import eaf.models.ClassType;
import eaf.models.FieldType;
import eaf.rects.Rect;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;


public class ClassRect extends RectWithRects {

    public ClassRect(int width, int height, Color color, ClassType type, String[] names, Rect[] rects, FieldType[] types, boolean locked) {
        super(width, height, color, type, names, rects, types, locked);
    }



    @Override
    public Rect clone() {
        return new ClassRect(realWidth(), realHeight(), color, getClazz(), names, subRects, types, locked);
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
            var n = SyntaxTree.toSimpleName(clazz.name);
            g.drawString(n, x + spacing, y + spacing + fontSize);
            registerString(n, y + spacing);
        }
    };

    @Override
    public int extraSpacingBelow() {
        return 0;
    }

    @Override
    public String toString(int level) {
        String res = "'" + SyntaxTree.toSimpleName(clazz.name) + "' {";
        boolean test = subRects.length > 0;
        if (test) {
            res += "\n";
        }
        int i = 0;
        for (var t : clazz.fields.entrySet()) {
            res += repeatString(stringPadding, level + 1 ) + "'" + t.getKey() + "' := " + subRects[i].toString(level + 1);
            res += ";\n";
            i++;
        }
        if (test) {
            res += repeatString(stringPadding, level);
        }
        res += "}";
        return res;
    }

}
