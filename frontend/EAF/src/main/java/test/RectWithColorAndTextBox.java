package test;

import javax.swing.*;
import java.awt.*;

class RectWithColorAndTextBox extends Rect {
    JTextField textBox;

    public RectWithColorAndTextBox(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
        textBox = new JTextField("Insert Text Here");
    }

    public RectWithColorAndTextBox(int x, int y, int width, int height, Color color, JTextField field) {
        super(x, y, width, height, color);
        textBox = new JTextField(field.getText());
    }

    @Override
    void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        textBox.setBounds(x + 2, y + 2, width - 4, height - 4);
    }

    @Override
    public Rect clone() {
        return new RectWithColorAndTextBox(x, y, width, height, color, textBox);
    }

    @Override
    public void addTo(JPanel p) {
        p.add(textBox);
    }

    @Override
    public void removeFrom(JPanel p) {
        p.remove(textBox);
    }
}