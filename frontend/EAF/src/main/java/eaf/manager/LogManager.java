package eaf.manager;

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

}

