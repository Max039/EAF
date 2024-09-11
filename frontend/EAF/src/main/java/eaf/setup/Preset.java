package eaf.setup;

import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

public abstract class Preset {



    public ArrayList<String> requiredRectNames;

    public static ArrayList<Preset> presets = new ArrayList<>();

    public static void prepareSetups() {
        presets.add(new EA());
        presets.add(new ML());
        presets.add(new Generator());
    }

    public abstract void generateFiles(String folder, RectPanel panel);
    public abstract String executionLine();
    public abstract boolean postStart();
    public abstract void postStop();
    public abstract String getName();
    public abstract String getDisplayName();

    public abstract boolean implementationError();

    public static Preset getPreset(String name) {
        for (var preset : presets) {
            if (preset.getName().equals(name)) {
                return preset;
            }
        }
        return null;
    }

    public abstract String shName();
}
