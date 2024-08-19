package eaf.ui.panels;

import eaf.models.Constant;
import eaf.compiler.SyntaxTree;
import eaf.Main;
import eaf.models.Pair;
import eaf.rects.Rect;
import eaf.rects.TextFieldRect;
import eaf.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstantPane extends JScrollPane {

    public static HashMap<String, Constant> additionalConstants = new HashMap<>();
    public static HashMap<String, Constant> constants;

    private JPanel contentPanel;

    public ConstantPane() {
        contentPanel = new JPanel();
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
        constants.putAll(SyntaxTree.constantRegister);
        constants.putAll(additionalConstants);
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
        gbc.insets = new Insets(0, 5, 0, 5);  // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1.0;  // Ensure the columns resize horizontally
        contentPanel.add(new JLabel("Name"), gbc);

        gbc.gridx++;
        contentPanel.add(new JLabel("Type"), gbc);

        gbc.gridx++;
        contentPanel.add(new JLabel("Value"), gbc);

        gbc.gridx++;
        contentPanel.add(new JLabel("Package"), gbc);

        gbc.gridx++;
        gbc.weightx = 0;  // Prevent the button column from resizing
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> openAddConstantWindow());
        contentPanel.add(addButton, gbc);
    }

    private void addConstantRows() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;  // Start after the header
        gbc.insets = new Insets(0, 5, 0, 5);  // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;  // Make the columns expand to fill the available space

        for (var entry : constants.entrySet()) {
            gbc.gridx = 0;
            String name = entry.getKey();
            Constant constant = entry.getValue();

            contentPanel.add(new JLabel(name), gbc);

            gbc.gridx++;
            contentPanel.add(new JLabel(constant.getType()), gbc);

            gbc.gridx++;
            contentPanel.add(new JLabel(constant.getValue()), gbc);

            gbc.gridx++;
            contentPanel.add(new JLabel(constant.pack), gbc);

            gbc.gridx++;
            gbc.weightx = 0;  // Prevent the button column from resizing
            JButton removeButton = new JButton("-");
            removeButton.setEnabled(additionalConstants.containsKey(name));
            removeButton.addActionListener(e -> confirmAndRemoveConstant(name));
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

                // If all checks pass, add the constant
                additionalConstants.put(name, new Constant(name, value, type, ""));
                refreshUI();
            }
        } while (result == JOptionPane.OK_OPTION && (nameField.getText().trim().isEmpty() || valueField.getText().trim().isEmpty() || constants.containsKey(nameField.getText().trim()) || additionalConstants.containsKey(nameField.getText().trim())));
    }

    private void confirmAndRemoveConstant(String name) {
        int result = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to remove the constant '" + name + "'?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            additionalConstants.remove(name);
            refreshUI();
        }
    }

    public static ArrayList<Constant> getUsedConstants() {
        ArrayList<Constant> arr = new ArrayList<>();
        for (var r : Main.mainPanel.leftPanel.getRects()) {
            arr.addAll(getUsedConstants(r));
        }
        return arr;
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
}
