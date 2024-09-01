package eaf.intro;

import eaf.Main;

import javax.swing.*;
import java.awt.*;

public abstract class Intro extends JPanel {
    public String objective = "";

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public abstract void stop();
    public abstract boolean isUnfinished();

}

