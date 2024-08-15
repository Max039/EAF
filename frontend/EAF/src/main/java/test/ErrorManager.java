package test;

import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

import java.util.HashMap;

public class ErrorManager {
    public static HashMap<Rect, String> erroRects = new HashMap<>();

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
        clearErrors();
        System.out.println("Checking for errors");
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            checkForErrors(r);
        }

        for (var e : erroRects.values()) {
            System.out.println("Error in rect: " + e);
        }

    }
}
