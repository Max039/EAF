package eaf.manager;

import eaf.Main;

import java.awt.*;

public class ColorManager {

    //==========================================
    //                    Logging
    //==========================================
    public static Color infoColor = new Color(80, 200, 255);

    public static Color errorColor = new Color(255, 0, 0);
    public static Color warningColor = new Color(255, 255, 0);

    public static Color sucessColor = new Color(0, 255, 0);

    //==========================================
    //                    Syntax
    //==========================================

    public static Color syntaxColor = new Color(200,200, 255);

    public static Color importColor = new Color(70, 255, 70);

    public static Color fieldColor = new Color(0, 120, 255);

    public static Color typeColor = new Color(255, 170, 0);

    public static Color parsingColor = new Color(255, 0, 255);

    public static Color packageColor = new Color(150, 150, 255);

    //==========================================
    //                    Files
    //==========================================

    public static Color fileColor = new Color(255,200, 255);
    public static Color readColor = new Color(200,90, 80);
    public static Color writeColor = new Color(80,255, 170);

    //==========================================
    //               ErrorManager
    //==========================================

    public static Color errorManagerColor = new Color(200,0, 200);

    //==========================================
    //               FileManager
    //==========================================

    public static Color fileManagerColor = new Color(160,240, 180);

    //==========================================
    //               CacheManager
    //==========================================

    public static Color cacheManagerColor = new Color(255,240, 180);

    //==========================================
    //               RectFactory
    //==========================================

    public static Color rectFactoryColor = new Color(190,230, 240);

    //==========================================
    //               Script-Writer
    //==========================================

    public static Color scriptWriter = new Color(60,230, 240);

    public static Color script = new Color(140,70, 70);

    public static Color shell = new Color(160,90, 60);

    public static Color data = new Color(160,60, 90);

    public static Color ol = new Color(90,160, 60);

    //==========================================
    //               Executor
    //==========================================

    public static Color executor = new Color(90,120, 240);
    public static Color process = new Color(70,200, 70);

    public static Color log = new Color(200,200, 200);

    public static Color status = new Color(255,255, 255);


    //==========================================
    //               Plugin
    //==========================================

    public static Color plugin = new Color(190,230, 240);


    //==========================================
    //               Maven
    //==========================================

    public static Color maven = new Color(140,200, 255);

    //==========================================
    //               ClassLocator
    //==========================================

    public static Color classLocator = new Color(140,160, 170);

    //==========================================
    //               IntelliJ
    //==========================================

    public static Color intelliJ = new Color(255,160, 170);

    //==========================================
    //               Downloader
    //==========================================

    public static Color downloader = new Color(160,160, 170);

    //==========================================
    //               Main
    //==========================================

    public static Color main = new Color(30,160, 170);

    public static Color args = new Color(160,50, 170);

    //==========================================
    //               Preset
    //==========================================

    public static Color preset = new Color(60,160, 120);

    //==========================================
    //               Importer
    //==========================================

    public static Color importer = new Color(60,160, 120);


    //==========================================
    //==========================================


    public static String rgbToAnsi(int r, int g, int b) {
        // Ensure RGB values are within the 0-255 range
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        // ANSI escape code for RGB foreground color
        return String.format("\033[38;2;%d;%d;%dm", r, g, b);
    }

    public static String colorText(String text, int r, int g, int b) {
        String colorAnsi = rgbToAnsi(r, g, b);
        String resetAnsi = "\033[0m"; // Reset ANSI code to default color
        if (Main.ansi) {
            return colorAnsi + text + resetAnsi;
        }
        else {
            return text;
        }

    }

    public static String colorText(String text, Color c) {
        String colorAnsi = rgbToAnsi(c.getRed(), c.getGreen(), c.getBlue());
        String resetAnsi = "\033[0m"; // Reset ANSI code to default color
        if (Main.ansi) {
            return colorAnsi + text + resetAnsi;
        }
        else {
            return text;
        }
    }


}
