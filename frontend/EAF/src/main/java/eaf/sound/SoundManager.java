package eaf.sound;

import eaf.Main;

import java.awt.*;

public class SoundManager {

    public static void playExclamationSound() {
        String osName = System.getProperty("os.name").toLowerCase();


        switch (Main.os) {
            case MAC -> {
                // macOS specific implementation
                try {
                    // Play a sound or perform some action specific to macOS
                    java.awt.Toolkit.getDefaultToolkit().beep(); // Beep sound
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case WINDOWS -> {
                if (osName.contains("win")) {
                    ((Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation")).run();
                }
            }
        }


    }
}
