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

public class RectWithColorAndTextBox extends Rect {
    JTextField textBox;

    Color textColor = new Color(255, 255, 255);

    Color bgColor = new Color(55, 55, 55);

    Color borderColor = new Color(85, 85, 85);

    public static int spacing = 0;

    public RectWithColorAndTextBox(ClassType type) {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255), type);
        setTextBox("Insert Text Here");
    }

    public RectWithColorAndTextBox(int width, int height, Color color, ClassType type) {
        super(width, height, color, type);
        setTextBox("Insert Text Here");
    }

    public RectWithColorAndTextBox(int width, int height, Color color, ClassType type, JTextField field) {
        super(width, height, color, type);
        setTextBox(field.getText());
    }

    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
        adjustTextBoxColor();
    }

    private void adjustTextBoxColor() {
        textBox.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity()*255)));
        textBox.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(getOpacity()*255)));
        Color caret = textBox.getCaretColor();
        textBox.setCaretColor(new Color(caret.getRed(), caret.getGreen(), caret.getBlue(), (int)(getOpacity()*255)));
        Color selected = textBox.getSelectedTextColor();
        textBox.setSelectedTextColor(new Color(selected.getRed(), selected.getGreen(), selected.getBlue(), (int)(getOpacity()*255)));
        Color selection = textBox.getSelectionColor();
        textBox.setSelectionColor(new Color(selection.getRed(), selection.getGreen(), selection.getBlue(), (int)(getOpacity()*255)));
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
        adjustTextBoxColor();
    }

    @Override
    public void draw(Graphics g, double a) {
        var g2 = (Graphics2D) g;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());

        textBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }


    @Override
    public Rect clone() {
        return new RectWithColorAndTextBox(getWidth(), getHeight(), color, clazz, textBox);
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


}