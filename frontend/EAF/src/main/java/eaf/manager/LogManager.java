package eaf.manager;

import eaf.Main;

public class LogManager {

    public static String encased(String s) {
        return "[" + s + "]";
    }

    public static String error() {
        return encased(ColorManager.colorText("Error", ColorManager.errorColor));
    }

    public static String warning() {
        return encased(ColorManager.colorText("Warning", ColorManager.warningColor));
    }

    public static String info() {
        return encased(ColorManager.colorText("Info", ColorManager.infoColor));
    }


    public static String syntax() {
        return encased(ColorManager.colorText("Syntax", ColorManager.syntaxColor));
    }

    public static String type() {
        return syntax() + encased(ColorManager.colorText("Type", ColorManager.typeColor));
    }


    public static String imp() {
        return syntax() + encased(ColorManager.colorText("Import", ColorManager.importColor));
    }

    public static String field() {
        return syntax() + encased(ColorManager.colorText("Field", ColorManager.fieldColor));
    }

    public static String parsing() {
        return syntax() + encased(ColorManager.colorText("Parsing", ColorManager.parsingColor));
    }

    public static String pack() {
        return syntax() + encased(ColorManager.colorText("Package", ColorManager.packageColor));
    }


    public static String file() {
        return encased(ColorManager.colorText("File", ColorManager.fileColor));
    }

    public static String read() {
        return file() + encased(ColorManager.colorText("Read", ColorManager.readColor));
    }

    public static String write() {
        return file() + encased(ColorManager.colorText("Write", ColorManager.writeColor));
    }

    public static String errorManager() {
        return encased(ColorManager.colorText("Error-Manager", ColorManager.errorManagerColor));
    }

    public static String fileManager() {
        return encased(ColorManager.colorText("File-Manager", ColorManager.fileManagerColor));
    }

    public static String cacheManager() {
        return encased(ColorManager.colorText("Cache-Manager", ColorManager.cacheManagerColor));
    }

    public static String reactFactory() {
        return encased(ColorManager.colorText("Rect-Factory", ColorManager.rectFactoryColor));
    }

    public static String scriptWriter() {
        return encased(ColorManager.colorText("Script-Writer", ColorManager.scriptWriter));
    }

    public static String executor() {
        return encased(ColorManager.colorText("Executor", ColorManager.executor));
    }

    public static String process() {
        return encased(ColorManager.colorText("Process", ColorManager.process));
    }

    public static String status() {
        return encased(ColorManager.colorText("Status", ColorManager.status));
    }

    public static String log() {
        return encased(ColorManager.colorText("Log", ColorManager.log));
    }

    public static String script() {
        return encased(ColorManager.colorText("Script", ColorManager.log));
    }

    public static String data() {
        return encased(ColorManager.colorText("Data-File", ColorManager.data));
    }


    public static String ol() {
        return encased(ColorManager.colorText("Ol-File", ColorManager.ol));
    }

    public static String shell() {
        return encased(ColorManager.colorText("Shell", ColorManager.shell));
    }

    public static String plugin() {
        return encased(ColorManager.colorText("Plugin", ColorManager.plugin));
    }

    public static String maven() {
        return encased(ColorManager.colorText("Maven", ColorManager.maven));
    }

    public static String classLocator() {
        return encased(ColorManager.colorText("Class-Locator", ColorManager.classLocator));
    }
    public static String intellij() {
        return encased(ColorManager.colorText("IntelliJ IDEA", ColorManager.intelliJ));
    }

    public static String downloader() {
        return encased(ColorManager.colorText("Downloader", ColorManager.downloader));
    }

    public static String main() {
        return encased(ColorManager.colorText("Main", ColorManager.main));
    }

    public static String args() {
        return encased(ColorManager.colorText("Args", ColorManager.args));
    }

    public static String preset() {
        return encased(ColorManager.colorText("Preset", ColorManager.preset));
    }

    public static void println(String s) {
        if (Main.fulllog) {
            System.out.println(s);
        }
    }

    public static void printf(String format, Object ... s) {
        if (Main.fulllog) {
            System.out.printf(format, s);
        }
    }

    public static void println() {
        if (Main.fulllog) {
            System.out.println();
        }
    }

    public static void print(String s) {
        if (Main.fulllog) {
            System.out.print(s);
        }
    }

}

