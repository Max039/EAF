package eaf.ui;

import eaf.Main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;

public class CustomMenuItemUI extends BasicMenuItemUI {
    private Color backgroundColor; // Default background color

    public CustomMenuItemUI(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(Main.dividerColor);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }
}
