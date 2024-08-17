package test;

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

}
