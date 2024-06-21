package test;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

class RectWithColorAndTextBox extends Rect {
    JTextField textBox;

    Graphics gr = null;

    public static int spacing = 5;

    public RectWithColorAndTextBox(int width, int height, Color color) {
        super(width, height, color);
        textBox = new JTextField("Insert Text Here");
        textBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                DragDropRectanglesWithSplitPane.mainFrame.repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                DragDropRectanglesWithSplitPane.mainFrame.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                DragDropRectanglesWithSplitPane.mainFrame.repaint();
            }
        });
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
    public int getWidth() {
        // Get the font metrics for the font used in the JTextField
        FontMetrics fontMetrics = textBox.getFontMetrics(textBox.getFont());

        // Get the text from the JTextField
        String text = textBox.getText();

        Insets insets = textBox.getInsets();
        int padding = insets.left + insets.right;

        // Measure the width of the text
        int textWidth = (int) (fontMetrics.stringWidth(text) + padding * 1.5F + spacing * 2);

        return Math.max(super.getWidth(), textWidth);

    }


    @Override
    public void removeFrom(JPanel p) {
        p.remove(textBox);
    }
}