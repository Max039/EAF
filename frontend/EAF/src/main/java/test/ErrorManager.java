package test;

import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import java.awt.*;
import java.util.HashMap;

public class ErrorManager {
    public static HashMap<Rect, Pair<Integer, String>> erroRects = new HashMap<>();
    public static HashMap<Rect, Pair<Integer, String>> warningRects = new HashMap<>();

    public static int errors = 0;

    public static int warnings = 0;

    public static int first = 0;

    public static Color errorColor = new Color(255, 0, 0);
    public static Color warningColor = new Color(255, 255, 0);

    public static Color saveColor = new Color(0, 255, 0);

    public static void clearErrors() {
        erroRects = new HashMap<>();
        warningRects = new HashMap<>();
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
        for (var e : warningRects.values()) {
            System.out.println(GuiCreator.colorText("Warning", warningColor) + ": " + e.getSecond());
        }
        for (var e : erroRects.values()) {
            System.out.println(GuiCreator.colorText("Error", errorColor) + ": " + e.getSecond());
        }
        System.out.println("======================");
        if (warningRects.isEmpty()) {
            System.out.println(GuiCreator.colorText("No Warnings!", saveColor));
        }
        else {
            System.out.println(GuiCreator.colorText("Total Warnings", warningColor) + ": " + warningRects.size());
        }
        if (erroRects.isEmpty()) {
            System.out.println(GuiCreator.colorText("No Errors!", saveColor));
        }
        else {
            System.out.println( GuiCreator.colorText("Total Errors", errorColor) + ": " + erroRects.size());
        }
        System.out.println("======================");
        warnings = warningRects.size();
        errors = erroRects.size();

        if (!erroRects.isEmpty()) {
            first = erroRects.values().stream().map(Pair::getFirst).min(Integer::compareTo).get();;
        }
    }
}
