package eaf.ui;

import eaf.Main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuBarUI;
import java.awt.*;

public class CustomMenuBarUI extends BasicMenuBarUI {
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setBackground(Main.dividerColor); // Set your desired background color here
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(Main.dividerColor); // Set your desired background color here
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }
}
