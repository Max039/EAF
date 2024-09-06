package eaf.setup;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.process.GenerationTracker;
import eaf.rects.Rect;
import eaf.ui.panels.ConstantPane;
import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

import static eaf.models.ClassType.getUniqueImports;

public class EA extends Preset {

    public EA() {
        requiredRectNames = new ArrayList<>();
        requiredRectNames.add("problem");
        requiredRectNames.add("evolutionary-algorithm");
        requiredRectNames.add("documentor");
    }

    @Override
    public void generateFiles(String folder, RectPanel panel) {
        System.out.println(LogManager.preset() + LogManager.write() + LogManager.data() + " Writing EvoAl Data ...");
        FileManager.write(Main.dataPanel.toString(), folder + "/config.ddl");
        System.out.println(LogManager.preset() + LogManager.write() + LogManager.ol()  + " Writing EvoAl Script ...");
        FileManager.write(rectPanelConversion(panel), folder+ "/config.ol");
    }

    public String rectPanelConversion(RectPanel panel) {
        String res = "";
        String problemName = panel.getRects().get(0).clazz.name;;
        String problemContent = panel.getRects().get(0).toString(1).split(" ", 2)[1];
        String algorithmName = panel.getRects().get(1).clazz.name;
        String algorithmContent = panel.getRects().get(1).toString(1).split(" ", 2)[1];
        String documentors = panel.getRects().get(2).toString(1).split("\\{", 2)[1];
        documentors = documentors.substring(0, documentors.length() - 2).replace("'documentors'", "documenting");
        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();
        for (var r : panel.getRects()) {
            panel.getAllRects(classesNeededForScript, r);
        }

        String constants = "";
        var imports = getUniqueImports(classesNeededForScript);
        for (var r : ConstantPane.getUsedConstants()) {
            if (!r.pack.isEmpty()) {
                if (!imports.contains(r.pack)) {
                    imports += "import \"definitions\" from " + r.pack + ";\n";
                }
            }
            else {
                constants += Rect.stringPadding + "const " + r.type + " " + r.name + " := " + r.value + ";\n";
            }

        }
        if (!constants.isEmpty()) {
            constants = "\n" + constants + "\n";
        }

        res += imports + "\n";
        res += "import \"data\" from 'config';\n\n";
        res += "module 'config' {\n";
        res += constants;
        res += Rect.stringPadding + "specify problem '" + problemName + "' ";
        res += problemContent;
        res += "\n\n\n";
        res += Rect.stringPadding + "configure '" + algorithmName + "' for '" + problemName + "' ";
        res += algorithmContent;
        StringBuilder sb = new StringBuilder(res);
        sb.insert(res.length() - 2, documentors);
        res = sb.toString();
        res += "}";

        return res;
    }

    @Override
    public String executionLine() {
        return "$SHELL $EVOAL_HOME/bin/evoal-search.sh . config.ol output";
    }

    @Override
    public boolean postStart() {
        try {
            GenerationTracker.connect();
            System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Connected to EvoAl!");
            return true;
        } catch (Exception ignored) {
            // Handle exception as necessary
            return false;
        }
    }

    @Override
    public void postStop() {
        System.out.println(LogManager.executor() + LogManager.process() + LogManager.status() + " Eaf closing port!");
        try {
            GenerationTracker.disconnect();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String getName() {
        return "ea";
    }
}
