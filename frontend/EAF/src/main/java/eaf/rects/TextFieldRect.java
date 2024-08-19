package eaf.rects;

import eaf.action.TextFieldAction;
import eaf.input.InputHandler;
import eaf.models.ClassType;
import org.json.JSONObject;
import eaf.*;
import eaf.models.Pair;
import eaf.ui.panels.ConstantPane;
import eaf.ui.panels.ErrorPane;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFieldRect extends Rect {
    public JTextField textBox;

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public static  Color defaultTextColor = new Color(255, 255, 255);


    Color  textColor = defaultTextColor;

    public static Color  bgColor = new Color(55, 55, 55);

    public static Color  borderColor = new Color(85, 85, 85);

    public static Color selectedColor = new Color(0, 0, 0);

    public static Color selectionColor = new Color(255, 255, 255);

    public static Color uneditableColor = new Color(151, 111, 151);

    private String snapshotText = ""; // Store the snapshot of the text
    boolean editable;

    public static int spacing = 0;

    public TextFieldRect(String content, int width, int height, Color color, ClassType type, boolean editable) {
        super(width, height, color, type);
        this.editable = editable;
        setTextBox(content);
    }

    public TextFieldRect(int width, int height, Color color, ClassType type, JTextField field, boolean editable, Color textColor) {
        super(width, height, color, type);
        this.editable = editable;
        this.textColor = textColor;
        setTextBox(field.getText());
    }

    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
        adjustTextBoxColor();
    }

    private void adjustTextBoxColor() {
        if (selected) {
            textBox.setBackground(new Color(selectedRectColor.getRed(), selectedRectColor.getGreen(), selectedRectColor.getBlue(), (int)(getOpacity() * 255)));
        }
        else {
            textBox.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity() * 255)));
        }

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
                setValidity();
                InputHandler.actionHandler.changesMade();
                Main.mainFrame.repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValidity();
                InputHandler.actionHandler.changesMade();
                Main.mainFrame.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValidity();
                InputHandler.actionHandler.changesMade();
                Main.mainFrame.repaint();
            }
        });

        textBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                snapshotText = textBox.getText(); // Snapshot the text when focus is gained
            }

            @Override
            public void focusLost(FocusEvent e) {
                String newText = textBox.getText();
                if (!snapshotText.equals(newText)) {
                    InputHandler.actionHandler.action(new TextFieldAction(TextFieldRect.this, snapshotText, newText));
                }
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
        if (selected) {
            g2.setColor(new Color(selectedRectColor.getRed(), selectedRectColor.getGreen(), selectedRectColor.getBlue(), (int)(255 * a)));
        }
        else {
            if (!valid) {
                g2.setColor(new Color(errorColor.getRed(), errorColor.getGreen(), errorColor.getBlue(), (int)(255 * a)));
            }
            else {
                if (warning) {
                    g2.setColor(new Color(warningColor.getRed(), warningColor.getGreen(), warningColor.getBlue(), (int)(255 * a)));
                }
                else {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * a)));
                }

            }

        }
        g2.fillRect(getX(), getY(), getWidth(), getHeight());

        textBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
        registerString(textBox.getText(), textBox.getY());
    }


    @Override
    public Rect clone() {
        return new TextFieldRect(getWidth(), getHeight(), color, clazz, textBox, editable, textColor);
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
        ErrorPane.erroRects.remove(this);
    }

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {

    };

    @Override
    public void setValidity() {
        ErrorPane.erroRects.remove(this);
        ErrorPane.warningRects.remove(this);
        String fieldName = parent.names[parentIndex];
        valid = true;
        warning = false;
        if (clazz.name.contains("string")) {
            warning = textBox.getText().isBlank();
            if (warning) {
                ErrorPane.warningRects.put(this, new Pair<>(getY(), fieldName + ": String is empty!"));
                return;
            }

            warning = textBox.getText().equals("Enter String here!");
            if (warning) {
                ErrorPane.warningRects.put(this, new Pair<>(getY(), fieldName + ": Default String!"));
            }
        }
        else if (clazz.name.contains("literal")) {
            warning = textBox.getText().equals("Enter Literal here!");
            if (warning) {
                ErrorPane.warningRects.put(this, new Pair<>(getY(), fieldName + ": Default literal!"));
            }
        }
        else {
            valid = !textBox.getText().isEmpty();
            if (!valid) {
                ErrorPane.erroRects.put(this, new Pair<>(getY(), fieldName + ": Field is empty!"));
                return;
            }
            boolean ty = clazz.name.contains("real");
            valid = isValidFormat(textBox.getText(), ty, new ArrayList<>(ConstantPane.getConstantsByType(clazz.name).stream().map(Pair::getFirst).toList()));
            if (!valid) {
                ErrorPane.erroRects.put(this, new Pair<>(getY(), fieldName + ": Input is not valid!"));
                return;
            }

            warning = textBox.getText().equals("0");
            if (warning) {
                ErrorPane.warningRects.put(this, new Pair<>(getY(), fieldName + ": Number value 0!"));
                return;
            }
        }

    };

    public static boolean isValidFormat(String input, boolean allowFloatingPoint, ArrayList<String> constants) {
        // Create regex pattern based on the value of allowFloatingPoint
        String numberPattern = allowFloatingPoint ? "\\d*\\.\\d+|\\d+" : "\\d+";
        String constantsPattern = String.join("|", constants);

        // Build the regex pattern
        String regex = "^(" + numberPattern + "|" + constantsPattern + ")([+\\-*/](" + numberPattern + "|" + constantsPattern + "))*$";

        // Compile and match the pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }


    @Override
    public void ifInvalid() {

    };

    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("type", "text-field");
        o.put("sub-type", clazz.name);
        o.put("value", textBox.getText());
        o.put("editable", editable);
        return  o;
    }

    @Override
    public String toString(int level) {
        return textBox.getText();
    }

}