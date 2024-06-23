package test.rects;

import test.DragDropRectanglesWithSplitPane;
import test.rects.multi.ArrayRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class RectWithColorAndTextBox extends Rect {
    JTextField textBox;

    public static int spacing = 5;

    public RectWithColorAndTextBox() {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255));
        setTextBox("Insert Text Here");
    }

    public RectWithColorAndTextBox(int width, int height, Color color) {
        super(width, height, color);
        setTextBox("Insert Text Here");
    }

    public RectWithColorAndTextBox(int width, int height, Color color, JTextField field) {
        super(width, height, color);
        setTextBox(field.getText());
    }

    public void setTextBox(String input) {
        textBox = new JTextField(input);
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

    @Override
    public void draw(Graphics g, double a) {
        var g2 = (Graphics2D) g;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getGreen(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());
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
    public boolean onHover(Point p) {
        return true;
    };

    @Override
    public void onMouseReleased() {

    };

    @Override
    public void removeFrom(JPanel p) {
        p.remove(textBox);
    }

    @Override
    public Rect newInstance() {
        return new RectWithColorAndTextBox();
    }
}