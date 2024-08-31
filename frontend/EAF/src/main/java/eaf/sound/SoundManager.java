package eaf.sound;

import eaf.Main;

import java.awt.*;
import java.io.IOException;

public class SoundManager {

    public static void playExclamationSound() {
        String osName = System.getProperty("os.name").toLowerCase();


        switch (Main.os) {
            case MAC -> {
                try {
                    Runtime.getRuntime().exec(new String[]{"osascript", "-e", "set volume output volume 100", "-e", "do shell script \"afplay /System/Library/Sounds/Funk.aiff\""});
                } catch (IOException e) {
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
