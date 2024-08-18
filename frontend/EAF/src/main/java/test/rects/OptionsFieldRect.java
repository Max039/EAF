package test.rects;

import action.OptionsFieldAction;
import compiler.ClassType;
import org.json.JSONObject;
import test.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OptionsFieldRect extends Rect {
    public JComboBox<String> comboBox;

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public static  Color defaultTextColor = new Color(255, 255, 255);

    Color textColor = defaultTextColor;
    public static Color bgColor = new Color(55, 55, 55);
    public static Color borderColor = new Color(85, 85, 85);

    public static Color selectedColor = new Color(0, 0, 0);


    Color invalidRectsColor = new Color(190, 70, 70);

    public static Color selectionColor = new Color(255, 255, 255);

    boolean editable;

    boolean first = true;

    public static int spacing = 0;

    public ArrayList<Object> options;

    String last = "";

    private String snapshotText = ""; // Store the snapshot of the text

    public OptionsFieldRect(ArrayList<Object> options, String selectedOption, int width, int height, Color color, ClassType type, boolean editable, Color textColor) {
        super(width, height, color, type);
        this.textColor = textColor;
        this.editable = editable;
        this.options = options;
        setComboBox(options, selectedOption);
    }

    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
        adjustComboBoxColor();
        setUi();
    }

    private void setUi() {
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                super.createArrowButton();
                JButton arrowButton = super.createArrowButton();
                arrowButton.setBorder(BorderFactory.createLineBorder(borderColor));
                arrowButton.setBackground(bgColor);
                arrowButton.setOpaque(true);
                return arrowButton;
            }

            // Add an ActionListener to refresh when an item is selected



            @Override
            protected ComboBoxEditor createEditor() {
                return new BasicComboBoxEditor() {
                    @Override
                    protected JTextField createEditorComponent() {
                        JTextField editor = super.createEditorComponent();
                        editor.setBorder(null);
                        editor.setOpaque(false);
                        return editor;
                    }
                };
            }
        });


        comboBox.repaint();
    }



    public void adjustComboBoxColor() {
        if (selected) {
            comboBox.setBackground(new Color(selectedRectColor.getRed(), selectedRectColor.getGreen(), selectedRectColor.getBlue(), (int)(getOpacity() * 255)));
        }
        else {
            if (valid) {
                if (warning) {
                    comboBox.setBackground(new Color(warningColor.getRed(), warningColor.getGreen(), warningColor.getBlue(), (int)(getOpacity() * 255)));
                }
                else {
                    comboBox.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity() * 255)));
                }
            }
            else {
                comboBox.setBackground(new Color(invalidRectsColor.getRed(), invalidRectsColor.getGreen(), invalidRectsColor.getBlue(), (int)(getOpacity() * 255)));
            }

        }

        comboBox.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(getOpacity() * 255)));

        Border border = BorderFactory.createLineBorder(borderColor, 1);
        comboBox.setBorder(border);
        comboBox.setOpaque(true);

        UIManager.put("ComboBox.selectionBackground", new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), (int)(getOpacity() * 255)));
        UIManager.put("ComboBox.selectionForeground", new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (int)(getOpacity() * 255)));
        UIManager.put("ComboBox.focus", new BorderUIResource.LineBorderUIResource(borderColor, 1));

        comboBox.putClientProperty("ComboBox.border", border);
        comboBox.putClientProperty("ComboBox.background", new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity() * 255)));
    }


    public void refreshComboBoxOptions() {
        setValidity();
        String selectedItem = (String) comboBox.getSelectedItem();

        // Update the comboBox model
        comboBox.setModel(new DefaultComboBoxModel<>(getOptions(selectedItem)));

        if (first) {
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // Snapshot the text when the menu is about to open
                    snapshotText = (String)comboBox.getSelectedItem();
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // Check for changes when the menu is about to close
                    String newText = (String)comboBox.getSelectedItem();
                    if (!snapshotText.equals(newText)) {
                        InputHandler.actionHandler.action(new OptionsFieldAction(OptionsFieldRect.this, snapshotText, newText));
                    }
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    // No action needed if the menu is canceled
                }
            });
            first = false;
        }


        // Restore the selected item
        comboBox.setSelectedItem(selectedItem);

        // Adjust the bounds and repaint
        comboBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
        comboBox.revalidate();
        comboBox.repaint();
    }

    public String[] getOptions(String selectedItem) {
        // Create an ArrayList to handle options and ensure no duplicates
        ArrayList<String> optionArrayList = new ArrayList<>();

        // Add all options to the ArrayList
        for (Object option : options) {
            String optionStr = option.toString();
            if (!optionArrayList.contains(optionStr)) {
                optionArrayList.add(optionStr);
            }
        }

        // Ensure the selected item is present in the options
        if (selectedItem != null && !optionArrayList.contains(selectedItem)) {
            optionArrayList.add(selectedItem);
        }

        // Convert ArrayList back to an array
        return optionArrayList.toArray(new String[0]);
    }

    public void setComboBox(ArrayList<Object> options, String selectedOption) {
        this.options = options;
        var t = getOptions(selectedOption);
        comboBox = new JComboBox<>(t);
        comboBox.setSelectedItem(selectedOption);
        comboBox.setOpaque(false);
        Border border = BorderFactory.createLineBorder(borderColor, 1);
        comboBox.setBorder(border);
        comboBox.addActionListener(e -> Main.mainFrame.repaint());
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        comboBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }

    @Override
    public void draw(Graphics g, double a) {
        if (!last.equals(comboBox.getSelectedItem())) {
            last = (String) comboBox.getSelectedItem();
            refreshComboBoxOptions();
        }

        adjustComboBoxColor();

        if (!valid) {
            ifInvalid();
        }

        var g2 = (Graphics2D) g;
        setColorBasedOnErrorAndWarning(this, g, a);
        g2.fillRect(getX(), getY(), getWidth(), getHeight());

        comboBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
        registerString((String)comboBox.getSelectedItem(), comboBox.getY());
    }

    @Override
    public Rect clone() {
        return new OptionsFieldRect(options, (String) comboBox.getSelectedItem(), getWidth(), getHeight(), color, clazz, editable, textColor);
    }

    @Override
    public void addTo(JPanel p) {
        p.add(comboBox);
    }

    @Override
    public int getWidth() {
        int comboBoxWidth = comboBox.getPreferredSize().width;
        return Math.max(super.getWidth(), comboBoxWidth);
    }

    @Override
    public Pair<Boolean, Boolean> onHover(Point p) {
        return new Pair<>(true, false);
    }

    @Override
    public void onMouseReleased() {
    }

    @Override
    public void removeFrom(JPanel p) {
        p.remove(comboBox);
        ErrorManager.erroRects.remove(this);
    }

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {
    }

    @Override
    public void setValidity() {
        ErrorManager.erroRects.remove(this);
        if (clazz.name.equals("data")) {
            boolean res = Main.dataPanel.getDataFieldList().stream().anyMatch(t -> ((DataField) t).getName().equals(comboBox.getSelectedItem()));
            if (!res) {
                String err = "Data with name \"" + comboBox.getSelectedItem() + "\" was not found!";
                ErrorManager.erroRects.put(this, new Pair<>(getY(), err));
            }
            valid = res;
            return;
        }

        valid = true;
    };

    @Override
    public void ifInvalid() {

    };


    @Override
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("type", "option-field");
        o.put("sub-type", clazz.name);
        o.put("value", comboBox.getSelectedItem());
        o.put("editable", editable);
        return  o;
    }

    @Override
    public String toString(int level) {
        if (clazz.name.equals("data")) {
            return "data '" + (String)comboBox.getSelectedItem() + "'";
        }
        else {
            return (String)comboBox.getSelectedItem();
        }
    }

}
