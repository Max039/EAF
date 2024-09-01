package eaf.ui;

import eaf.Main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuUI;
import java.awt.*;

public class CustomMenuUI extends BasicMenuUI {
    private final Color backgroundColor;
    private final Color foregroundColor;

    public CustomMenuUI(Color backgroundColor, Color foregroundColor) {
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(Main.dividerColor); // Set your desired background color here
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }


}
