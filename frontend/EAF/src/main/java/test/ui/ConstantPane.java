package test.ui;

import test.models.Constant;
import test.compiler.SyntaxTree;
import test.Main;
import test.models.Pair;
import test.rects.Rect;
import test.rects.TextFieldRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstantPane extends JScrollPane {


    public static HashMap<String, Constant> constants;
    public ConstantPane() {
        refreshConstants();
    }

    public static ArrayList<Pair<String, Constant>> getConstantsByType(String type) {
        refreshConstants();
        ArrayList<Pair<String, Constant>> arr = new ArrayList<>();
        for (var c : constants.entrySet()) {
            if (c.getValue().type.equals(type)) {
                arr.add(new Pair<>(c.getKey(), c.getValue()));
            }
        }
        return arr;
    }

    public static void refreshConstants() {
        constants = new HashMap<>();
        var reg = (HashMap<String, Constant>)SyntaxTree.constantRegister.clone();
        constants.putAll(reg);
    }

    public static ArrayList<Constant> getUsedConstants() {
        ArrayList<Constant> arr = new ArrayList<>();
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            arr.addAll(getUsedConstants(r));
        }
        return arr;
    }

    public static ArrayList<Constant> getUsedConstants(Rect r) {
        ArrayList<Constant> arr = new ArrayList<>();
        if (r instanceof RectWithRects) {
            for (var s : ((RectWithRects) r).getSubRects()) {
                arr.addAll(getUsedConstants(s));
            }
        }
        else if (r instanceof TextFieldRect) {
            String regex = "[*\\-+/]";
            var parts = ((TextFieldRect)r).textBox.getText().split(regex);
            for (var p : parts) {
                var res = constants.get(p);
                if (constants.get(p) != null) {
                    arr.add(res);
                }
            }
        }
        return arr;
    }
}
