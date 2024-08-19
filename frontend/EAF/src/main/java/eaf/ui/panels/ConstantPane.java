package eaf.ui.panels;

import eaf.action.ActionHandler;
import eaf.action.AddedConstantAction;
import eaf.action.RemovedConstantAction;
import eaf.input.InputHandler;
import eaf.models.Constant;
import eaf.compiler.SyntaxTree;
import eaf.Main;
import eaf.models.DataField;
import eaf.models.Pair;
import eaf.rects.Rect;
import eaf.rects.TextFieldRect;
import eaf.rects.multi.RectWithRects;
import eaf.ui.UiUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstantPane extends JScrollPane {

    public static HashMap<String, Constant> additionalConstants = new HashMap<>();
    public static HashMap<String, Constant> constants;

    private static final int ROW_HEIGHT = 30; // Fixed height for each row
    private JPanel contentPanel;

    public ConstantPane() {
        setBackground(Main.bgColor);
        setBorder(BorderFactory.createEmptyBorder());
        contentPanel = new JPanel();
        contentPanel.setBackground(Main.bgColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.setLayout(new GridBagLayout());  // Use GridBagLayout for flexibility
        setViewportView(contentPanel);
        refreshConstants();
    }

    public static ArrayList<Pair<String, Constant>> getConstantsByType(String type) {
        refreshConstants();
        ArrayList<Pair<String, Constant>> arr = new ArrayList<>();
        for (var c : constants.entrySet()) {
            if (c.getValue().type.equals(type)) {
                arr.add(new Pair<>(c.getKey(), c.getValue()));
            }
        }
        return arr;
    }

    public static void refreshConstants() {
        constants = new HashMap<>();
        constants.putAll(additionalConstants);
        constants.putAll(SyntaxTree.constantRegister);
    }

    public void refreshUI() {
        refreshConstants();
        contentPanel.removeAll();
        addHeaderRow();
        addConstantRows();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addHeaderRow() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);  // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1.0;  // Ensure the columns resize horizontally
        var name = new JLabel("Name");
        name.setForeground(DataFieldListPane.headColor);
        name.setBackground(DataFieldListPane.fieldColor);
        name.setOpaque(true);
        name.setBorder(new MatteBorder(1, 1, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
        name.setPreferredSize(new Dimension(30, ROW_HEIGHT));
        contentPanel.add(name, gbc);

        gbc.gridx++;
        var type = new JLabel("Type");
        type.setForeground(DataFieldListPane.typeColor);
        type.setBackground(DataFieldListPane.fieldColor);
        type.setOpaque(true);
        type.setBorder(new MatteBorder(1, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
        type.setPreferredSize(new Dimension(30, ROW_HEIGHT));
        contentPanel.add(type, gbc);

        gbc.gridx++;
        var value = new JLabel("Value");
        value.setForeground(DataFieldListPane.typeColor);
        value.setBackground(DataFieldListPane.fieldColor);
        value.setOpaque(true);
        value.setBorder(new MatteBorder(1, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
        value.setPreferredSize(new Dimension(80, ROW_HEIGHT));
        contentPanel.add(value, gbc);

        gbc.gridx++;
        var pack = new JLabel("Package");
        pack.setForeground(DataFieldListPane.typeColor);
        pack.setBackground(DataFieldListPane.fieldColor);
        pack.setOpaque(true);
        pack.setBorder(new MatteBorder(1, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
        pack.setPreferredSize(new Dimension(80, ROW_HEIGHT));
        contentPanel.add(pack, gbc);

        gbc.gridx++;
        gbc.weightx = 0;  // Prevent the button column from resizing
        JButton addButton = new JButton("+");
        addButton.setForeground(Main.searchBarText);
        addButton.setBackground(DataFieldListPane.fieldColor);
        addButton.setOpaque(true);
        addButton.setBorder(new MatteBorder(1, 0, 1, 1, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
        addButton.addActionListener(e -> openAddConstantWindow());
        addButton.setPreferredSize(new Dimension(80, ROW_HEIGHT));
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(addButton, gbc);
    }

    private void addConstantRows() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;  // Start after the header
        gbc.insets = new Insets(0, 0, 0, 0);  // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;  // Make the columns expand to fill the available space

        var sorted = constants.entrySet().stream().sorted((entry1, entry2) -> {
            boolean inMap2Entry1 = additionalConstants.containsKey(entry1.getKey());
            boolean inMap2Entry2 = additionalConstants.containsKey(entry2.getKey());

            // Primary sort: Entries that exist in map2 should come first
            if (inMap2Entry1 && !inMap2Entry2) {
                return -1;
            } else if (!inMap2Entry1 && inMap2Entry2) {
                return 1;
            } else {
                // Secondary sort: Sort by the package name (pack field)
                int packageComparison = entry1.getValue().pack.compareTo(entry2.getValue().pack);
                if (packageComparison != 0) {
                    return packageComparison;
                }

                // Tertiary sort: Sort by the constant name (name field)
                return entry1.getValue().getName().compareTo(entry2.getValue().getName());
            }
        });

        for (var entry : sorted.toList()) {
            gbc.gridx = 0;
            String n = entry.getKey();
            Constant constant = entry.getValue();

            var name = new JLabel(n);
            name.setForeground(Main.searchBarText);
            name.setBackground(DataFieldListPane.fieldColor);
            name.setOpaque(true);
            name.setBorder(new MatteBorder(0, 1, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
            name.setPreferredSize(new Dimension(30, ROW_HEIGHT));
            contentPanel.add(name, gbc);

            gbc.gridx++;
            var type = new JLabel(constant.getType());
            type.setForeground(Main.searchBarText);
            type.setBackground(DataFieldListPane.fieldColor);
            type.setOpaque(true);
            type.setBorder(new MatteBorder(0, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
            type.setPreferredSize(new Dimension(30, ROW_HEIGHT));
            contentPanel.add(type, gbc);

            gbc.gridx++;
            var value = new JLabel(constant.getValue());
            value.setForeground(Main.searchBarText);
            value.setBackground(DataFieldListPane.fieldColor);
            value.setOpaque(true);
            value.setBorder(new MatteBorder(0, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
            value.setPreferredSize(new Dimension(80, ROW_HEIGHT));
            contentPanel.add(value, gbc);

            gbc.gridx++;
            var pack = new JLabel(constant.pack);
            pack.setForeground(Main.searchBarText);
            pack.setBackground(DataFieldListPane.fieldColor);
            pack.setOpaque(true);
            pack.setBorder(new MatteBorder(0, 0, 1, 0, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
            pack.setPreferredSize(new Dimension(80, ROW_HEIGHT));
            contentPanel.add(pack, gbc);

            gbc.gridx++;
            gbc.weightx = 0;  // Prevent the button column from resizing
            JButton removeButton = new JButton("-");
            removeButton.setEnabled(additionalConstants.containsKey(n));
            if (additionalConstants.containsKey(n)) {
                removeButton.setForeground(DataFieldListPane.minusColor);
            }
            else {
                removeButton.setForeground(Main.searchBarText);
            }

            removeButton.setBackground(DataFieldListPane.fieldColor);
            removeButton.setOpaque(true);
            removeButton.setBorder(new MatteBorder(0, 0, 1, 1, DataFieldListPane.borderColor)); // White border (none, none, bottom, right)
            removeButton.setPreferredSize(new Dimension(80, ROW_HEIGHT));
            removeButton.addActionListener(e -> confirmAndRemoveConstant(n));
            gbc.weightx = 0.2;
            gbc.anchor = GridBagConstraints.EAST;
            contentPanel.add(removeButton, gbc);

            gbc.gridy++;  // Move to the next row
            gbc.weightx = 1.0;  // Reset for the next row
        }
    }

    private void openAddConstantWindow() {
        JTextField nameField = new JTextField();
        JTextField valueField = new JTextField();
        String[] types = {"real", "int", "string"};
        JComboBox<String> typeDropdown = new JComboBox<>(types);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Type:"));
        panel.add(typeDropdown);
        panel.add(new JLabel("Value:"));
        panel.add(valueField);

        int result;
        do {
            result = JOptionPane.showConfirmDialog(null, panel, "Add New Constant",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String type = (String) typeDropdown.getSelectedItem();
                String value = valueField.getText().trim();

                // Validation: Check if all fields are filled
                if (name.isEmpty() || value.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields must be filled out.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Validation: Check if the name is unique
                if (constants.containsKey(name) || additionalConstants.containsKey(name)) {
                    JOptionPane.showMessageDialog(null, "The name is already used. Please choose a different name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                var con = new Constant(name, value, type, "");

                // If all checks pass, add the constant
                additionalConstants.put(name, con);
                InputHandler.actionHandler.action(new AddedConstantAction(con));
                refreshUI();
                break;
            }
        } while (result == JOptionPane.OK_OPTION && (nameField.getText().trim().isEmpty() || valueField.getText().trim().isEmpty() || constants.containsKey(nameField.getText().trim()) || additionalConstants.containsKey(nameField.getText().trim())));
    }

    private void confirmAndRemoveConstant(String name) {
        int result = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to remove the constant '" + name + "'?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            InputHandler.actionHandler.action(new RemovedConstantAction(additionalConstants.get(name)));
            additionalConstants.remove(name);
            refreshUI();
        }
    }

    public static ArrayList<Constant> getUsedConstants() {
        ArrayList<Constant> arr = new ArrayList<>();
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            arr.addAll(getUsedConstants(r));
        }
        var uniques = new ArrayList<Constant>();

        for (var e : arr) {
            if(!uniques.contains(e))
                uniques.add(e);
        }
        return uniques;
    }

    public static ArrayList<Constant> getUsedConstants(Rect r) {
        ArrayList<Constant> arr = new ArrayList<>();
        if (r instanceof RectWithRects) {
            for (var s : ((RectWithRects) r).getSubRects()) {
                arr.addAll(getUsedConstants(s));
            }
        } else if (r instanceof TextFieldRect) {
            String regex = "[*\\-+/]";
            var parts = ((TextFieldRect) r).textBox.getText().split(regex);
            for (var p : parts) {
                var res = constants.get(p);
                if (constants.get(p) != null) {
                    arr.add(res);
                }
            }
        }
        return arr;
    }


    public JSONArray toJson() {
        var a = new JSONArray();
        for (var r : additionalConstants.values()) {
            a.put(r.toJson());
        }
        return a;
    }

    public void fromJson(JSONArray a) {
        additionalConstants = new HashMap<>();
        for (var r : a) {
            var r2 = (JSONObject) r;
            var c = new Constant(r2.getString("name"), r2.getString("value"), r2.getString("type"), "");
            additionalConstants.put(c.name, c);
        }
        refreshUI();
    }

    public void removeConstant(String name) {
        additionalConstants.remove(name);
        refreshUI();
    }

    public void addConstant(Constant c) {
        additionalConstants.put(c.name, c);
        refreshUI();
    }
}
