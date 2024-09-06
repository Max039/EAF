package eaf.setup;

import eaf.Main;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.process.GenerationTracker;
import eaf.ui.panels.RectPanel;

import java.util.ArrayList;

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
        FileManager.write(panel.toString(), folder+ "/config.ol");
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
}
