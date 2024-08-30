package eaf.sound;

import java.awt.*;

public class SoundManager {

    public static void playExclamationSound() {
        String osName = System.getProperty("os.name").toLowerCase();

        // Check if it contains "win"
        if (osName.contains("win")) {
            ((Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation")).run();
        }
    }
}
