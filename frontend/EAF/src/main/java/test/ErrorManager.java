package test;

import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

import java.util.HashMap;

public class ErrorManager {
    public static HashMap<Rect, Pair<Integer, String>> erroRects = new HashMap<>();
    public static HashMap<Rect, Pair<Integer, String>> warningRects = new HashMap<>();

    public static int errors = 0;

    public static int warnings = 0;

    public static int first = 0;



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
        System.out.println(LogManager.errorManager() + " Checking for errors");
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            checkForErrors(r);
        }
        for (var e : warningRects.values()) {
            System.out.println(LogManager.errorManager() + LogManager.error() + ": " + e.getSecond());
        }
        for (var e : erroRects.values()) {
            System.out.println(LogManager.errorManager() + LogManager.error() + ": " + e.getSecond());
        }
        System.out.println("======================");
        if (warningRects.isEmpty()) {
            System.out.println(LogManager.errorManager() + ColorManager.colorText(" No Warnings!", ColorManager.sucessColor));
        }
        else {
            System.out.println(LogManager.errorManager() + ColorManager.colorText(" Total Warnings", ColorManager.warningColor) + ": " + warningRects.size());
        }
        if (erroRects.isEmpty()) {
            System.out.println(LogManager.errorManager() + ColorManager.colorText(" No Errors!", ColorManager.sucessColor));
        }
        else {
            System.out.println(LogManager.errorManager() + ColorManager.colorText(" Total Errors", ColorManager.errorColor) + ": " + erroRects.size());
        }
        System.out.println("======================");
        warnings = warningRects.size();
        errors = erroRects.size();

        if (!erroRects.isEmpty()) {
            first = erroRects.values().stream().map(Pair::getFirst).min(Integer::compareTo).get();;
        }
    }
}
