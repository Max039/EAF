package test;

import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import java.util.HashMap;

public class ErrorManager {
    public static HashMap<Rect, Pair<Integer, String>> erroRects = new HashMap<>();

    public static int errors = 0;

    public static int first = 0;

    public static void clearErrors() {
        erroRects = new HashMap<>();
    }

    public static void checkForErrors(Rect r) {
        r.setValidity();
        if (r instanceof RectWithRects) {
            for (var t : ((RectWithRects) r).getSubRects()) {
                if (t != null) {
                    checkForErrors(t);
                }
            }
        } else if (r instanceof OptionsFieldRect) {
            ((OptionsFieldRect)r).refreshComboBoxOptions();
        }
    }

    public static void checkForErrors() {
        Main.mainPanel.leftPanel.forceAdjustRects();
        clearErrors();
        System.out.println("======================");
        System.out.println("Checking for errors");
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            checkForErrors(r);
        }

        for (var e : erroRects.values()) {
            System.out.println(e.getSecond());
        }
        System.out.println("======================");
        System.out.println("Total errors: " + erroRects.size());
        System.out.println("======================");
        errors = erroRects.size();
        if (!erroRects.isEmpty()) {
            first = erroRects.values().stream().map(Pair::getFirst).min(Integer::compareTo).get();;
        }
    }
}
