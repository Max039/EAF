package test.rects;

import compiler.ClassType;
import test.DragDropRectanglesWithSplitPane;
import test.Pair;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TextFieldRect extends Rect {
    JTextField textBox;

    Color textColor = new Color(255, 255, 255);

    Color bgColor = new Color(55, 55, 55);

    Color borderColor = new Color(85, 85, 85);

    Color selectedColor = new Color(0, 0, 0);

    Color selectionColor = new Color(255, 255, 255);

    Color uneditableColor = new Color(151, 111, 151);

    boolean editable;

    public static int spacing = 0;

    public TextFieldRect(String content, ClassType type, boolean editable) {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255), type);
        this.editable = editable;
        setTextBox(content);
    }

    public TextFieldRect(String content, int width, int height, Color color, ClassType type, boolean editable) {
        super(width, height, color, type);
        this.editable = editable;
        setTextBox(content);
    }

    public TextFieldRect(int width, int height, Color color, ClassType type, JTextField field, boolean editable) {
        super(width, height, color, type);
        this.editable = editable;
        setTextBox(field.getText());
    }

    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
        adjustTextBoxColor();
    }

    private void adjustTextBoxColor() {
        textBox.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity()*255)));
        if (textBox.isEditable()) {
            textBox.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(getOpacity()*255)));
        }
        else {
            textBox.setForeground(new Color(uneditableColor.getRed(), uneditableColor.getGreen(), uneditableColor.getBlue(), (int)(getOpacity()*255)));
        }
        textBox.setCaretColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(getOpacity()*255)));
        textBox.setSelectedTextColor(new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (int)(getOpacity()*255)));
        textBox.setSelectionColor(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), (int)(getOpacity()*255)));
        Color disabled = textBox.getDisabledTextColor();
        textBox.setDisabledTextColor(new Color(disabled.getRed(), disabled.getGreen(), disabled.getBlue(), (int)(getOpacity()*255)));
        textBox.revalidate();
    }

    public void setTextBox(String input) {
        textBox = new JTextField(input);
        textBox.setOpaque(false);
        Border border = BorderFactory.createLineBorder(borderColor, 1);
        textBox.setBorder(border);

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
        textBox.setEditable(editable);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        textBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }

    @Override
    public void draw(Graphics g, double a) {
        adjustTextBoxColor();

        if (!valid) {
            ifInvalid();
        }

        var g2 = (Graphics2D) g;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());

        textBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }


    @Override
    public Rect clone() {
        return new TextFieldRect(getWidth(), getHeight(), color, clazz, textBox, editable);
    }

    @Override
    public void addTo(JPanel p) {
        p.add(textBox);
    }
    @Override
    public int getWidth() {
        int textWidth = getTextWidth();

        return Math.max(super.getWidth(), textWidth);
    }

    public int getTextWidth() {
        // Get the font metrics for the font used in the JTextField
        FontMetrics fontMetrics = textBox.getFontMetrics(textBox.getFont());

        // Get the text from the JTextField
        String text = textBox.getText();

        Insets insets = textBox.getInsets();
        int padding = insets.left + insets.right;

        return (int) (fontMetrics.stringWidth(text) + padding * 1.5F + spacing * 2);
    }

    @Override
    public Pair<Boolean, Boolean> onHover(Point p) {
        return new Pair<>(true, false);
    };

    @Override
    public void onMouseReleased() {

    };

    @Override
    public void removeFrom(JPanel p) {
        p.remove(textBox);
    }

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {

    };

    @Override
    public Pair<Boolean, String> setValidity() {
        return new Pair<>(true, "");
    };

    @Override
    public void ifInvalid() {

    };

}