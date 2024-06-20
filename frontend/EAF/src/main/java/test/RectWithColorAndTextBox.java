package test;

import javax.swing.*;
import java.awt.*;

class RectWithColorAndTextBox extends Rect {
    JTextField textBox;

    public static int spacing = 5;

    public RectWithColorAndTextBox(int width, int height, Color color) {
        super(width, height, color);
        textBox = new JTextField("Insert Text Here");
    }

    public RectWithColorAndTextBox(int width, int height, Color color, JTextField field) {
        super(width, height, color);
        textBox = new JTextField(field.getText());
    }

    @Override
    void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        textBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }

    @Override
    public Rect clone() {
        return new RectWithColorAndTextBox(getWidth(), getHeight(), color, textBox);
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