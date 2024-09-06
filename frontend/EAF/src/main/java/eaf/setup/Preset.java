package eaf.setup;

import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

public abstract class Preset {

    public boolean implementationError = true;

    public ArrayList<String> requiredRectNames;

    public abstract void generateFiles(String folder, RectPanel panel);
    public abstract String executionLine();
    public abstract boolean postStart();
    public abstract void postStop();

}
