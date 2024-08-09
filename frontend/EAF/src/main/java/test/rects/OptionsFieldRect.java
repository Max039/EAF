package test.rects;

import compiler.ClassType;
import test.DragDropRectanglesWithSplitPane;
import test.Pair;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OptionsFieldRect extends Rect {
    JComboBox<String> comboBox;

    Color textColor = new Color(255, 255, 255);
    Color bgColor = new Color(55, 55, 55);
    Color borderColor = new Color(85, 85, 85);

    Color selectedColor = new Color(0, 0, 0);

    Color selectionColor = new Color(255, 255, 255);

    boolean editable;

    public static int spacing = 0;

    public ArrayList<Object> options;

    public OptionsFieldRect(ArrayList<Object> options, String selectedOption, ClassType type, boolean editable) {
        super(50, RectWithRects.emptyRowSize, new Color(255, 255, 255), type);
        this.editable = editable;
        this.options = options;
        setComboBox(options, selectedOption);
    }

    public OptionsFieldRect(ArrayList<Object> options, String selectedOption, int width, int height, Color color, ClassType type, boolean editable) {
        super(width, height, color, type);
        this.editable = editable;
        this.options = options;
        setComboBox(options, selectedOption);
    }

    public OptionsFieldRect(int width, int height, Color color, ClassType type, JComboBox<String> comboBox, boolean editable) {
        super(width, height, color, type);
        this.editable = editable;
        this.comboBox = comboBox;
        adjustComboBoxColor();
    }

    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);
        adjustComboBoxColor();
    }

    private void adjustComboBoxColor() {
        comboBox.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity() * 255)));
        comboBox.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(getOpacity() * 255)));

        Border border = BorderFactory.createLineBorder(borderColor, 1);
        comboBox.setBorder(border);
        comboBox.setOpaque(true);

        UIManager.put("ComboBox.selectionBackground", new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), (int)(getOpacity() * 255)));
        UIManager.put("ComboBox.selectionForeground", new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (int)(getOpacity() * 255)));
        UIManager.put("ComboBox.focus", new BorderUIResource.LineBorderUIResource(borderColor, 1));

        comboBox.putClientProperty("ComboBox.border", border);
        comboBox.putClientProperty("ComboBox.background", new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(getOpacity() * 255)));

        // Custom UI to remove the border around the arrow button
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = super.createArrowButton();
                arrowButton.setBorder(BorderFactory.createLineBorder(borderColor));
                arrowButton.setBackground(bgColor);
                arrowButton.setOpaque(true);
                return arrowButton;
            }

            @Override
            protected ComboPopup createPopup() {
                ComboPopup popup = super.createPopup();
                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        refreshComboBoxOptions();
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });
                return popup;
            }

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

    private void refreshComboBoxOptions() {
        String selectedItem = (String) comboBox.getSelectedItem();
        comboBox.setModel(new DefaultComboBoxModel<>(options.stream().map(Object::toString).toArray(String[]::new)));
        comboBox.setSelectedItem(selectedItem);
    }

    public void setComboBox(ArrayList<Object> options, String selectedOption) {
        comboBox = new JComboBox<>(options.stream().map(Object::toString).toArray(String[]::new));
        comboBox.setSelectedItem(selectedOption);
        comboBox.setOpaque(false);
        Border border = BorderFactory.createLineBorder(borderColor, 1);
        comboBox.setBorder(border);
        comboBox.addActionListener(e -> DragDropRectanglesWithSplitPane.mainFrame.repaint());

        adjustComboBoxColor();
    }

    @Override
    public void draw(Graphics g, double a) {
        var g2 = (Graphics2D) g;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * a)));
        g2.fillRect(getX(), getY(), getWidth(), getHeight());

        comboBox.setBounds(getX() + spacing, getY() + spacing, getWidth() - spacing * 2, getHeight() - spacing * 2);
    }

    @Override
    public Rect clone() {
        return new OptionsFieldRect(options, (String) comboBox.getSelectedItem(), getWidth(), getHeight(), color, clazz, editable);
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
    }

    @Override
    public void onMouseClicked(boolean left, Point p, Point p2, MouseEvent e) {
    }
}
