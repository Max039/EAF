package test;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DataFieldListPane extends JScrollPane {
    private JPanel panel;
    private ArrayList<Object> dataFieldList;
    private static final int ROW_HEIGHT = 30; // Fixed height for each row

    public static Color fieldColor = new Color(43, 43, 43, 255);

    public Color borderColor = new Color(85, 85, 85);
    public static Color nameColor = new Color(255, 255, 255);

    public static Color headColor = new Color(203, 116, 47);

    public static Color typeColor = new Color(104, 151, 187);

    public static Color bgColor = new Color(49, 51, 53);

    public static Color minusColor = new Color(210, 100, 100);

    public DataFieldListPane() {
        dataFieldList = new ArrayList<>();
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor); // Set black background
        setViewportView(panel);
        setBorder(BorderFactory.createEmptyBorder());

        // Add header row with "Data Fields" and "+"
        createHeader();
    }

    private void createHeader() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0); // No padding
        gbc.gridy = 0;

        // Name label
        JLabel nameLabel = new JLabel("Data Name");
        nameLabel.setOpaque(true);
        nameLabel.setBackground(fieldColor);
        nameLabel.setPreferredSize(new Dimension(100, ROW_HEIGHT)); // Fixed size for the name column
        nameLabel.setForeground(headColor); // White text color
        nameLabel.setBorder(new MatteBorder(0, 1, 1, 0, borderColor)); // White border (none, left, bottom, none)
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        panel.add(nameLabel, gbc);

        // Type label
        JLabel typeLabel = new JLabel("Data Type");
        typeLabel.setOpaque(true);
        typeLabel.setBackground(fieldColor);
        typeLabel.setPreferredSize(new Dimension(100, ROW_HEIGHT)); // Fixed size for the type column
        typeLabel.setForeground(typeColor); // White text color
        typeLabel.setBorder(new MatteBorder(0, 0, 1, 0, borderColor)); // White border (none, none, bottom, none)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        panel.add(typeLabel, gbc);

        // "+" button
        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(50, ROW_HEIGHT)); // Fixed size for the button
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        addButton.setBorder(new MatteBorder(0, 0, 1, 1, borderColor)); // White border (none, none, bottom, right)
        panel.add(addButton, gbc);

        addButton.setBackground(fieldColor);
        addButton.setForeground(nameColor);
        addButton.setFocusPainted(false);

        // Add button action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopupMenu(addButton);
            }
        });
    }

    private void showPopupMenu(Component invoker) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem createNumberItem = new JMenuItem("Create Number");
        createNumberItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateNumberDialog();
            }
        });
        popupMenu.add(createNumberItem);

        JMenuItem createInstanceItem = new JMenuItem("Create Instance");
        createInstanceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateInstanceDialog();
            }
        });
        popupMenu.add(createInstanceItem);

        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddDataFieldDialog();
            }
        });
        popupMenu.add(newItem);

        popupMenu.show(invoker, invoker.getWidth() / 2, invoker.getHeight() / 2);
    }

    private void openCreateNumberDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Number", true);
        dialog.setLayout(new GridLayout(2, 2));
        dialog.setSize(300, 100);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                if (!name.isEmpty()) {
                    addDataField(new DataField(name, "quotient real", false));
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Name must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(okButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openCreateInstanceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Instance", true);
        dialog.setLayout(new GridLayout(3, 2));
        dialog.setSize(300, 150);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        JTextField typeField = new JTextField();

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String type = typeField.getText();
                if (!name.isEmpty() && !type.isEmpty()) {
                    addDataField(new DataField(name, type, true));
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Both fields must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(typeLabel);
        dialog.add(typeField);
        dialog.add(okButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAddDataFieldDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Data Field", true);
        dialog.setLayout(new GridLayout(4, 2));
        dialog.setSize(300, 150);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        JTextField typeField = new JTextField();

        JLabel instanceLabel = new JLabel("Instance:");
        JCheckBox instanceCheckBox = new JCheckBox();

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String type = typeField.getText();
                boolean instance = instanceCheckBox.isSelected();

                if (!type.isEmpty() && !name.isEmpty()) {
                    addDataField(new DataField(name, type, instance));
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Both fields must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(typeLabel);
        dialog.add(typeField);
        dialog.add(instanceLabel);
        dialog.add(instanceCheckBox);
        dialog.add(okButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void addDataField(DataField dataField) {
        dataFieldList.add(dataField);
        addDataFieldComponent(dataField);
        panel.revalidate();
        panel.repaint();
    }

    private void addDataFieldComponent(DataField dataField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0); // No padding
        gbc.gridy = panel.getComponentCount() - 1; // Position below existing components (before spacer)
        gbc.weighty = 0;

        // Name label
        JLabel nameLabel = new JLabel(dataField.getName());
        nameLabel.setOpaque(true);
        nameLabel.setBackground(fieldColor);
        nameLabel.setPreferredSize(new Dimension(100, ROW_HEIGHT)); // Fixed size for the name column
        nameLabel.setForeground(nameColor); // White text color
        nameLabel.setBorder(new MatteBorder(0, 1, 1, 0, borderColor)); // White border (none, left, bottom, none)
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        panel.add(nameLabel, gbc);

        String s;
        if (dataField.isInstance()) {
            s = "instance " + dataField.getType();
        } else {
            s = dataField.getType();
        }

        // Type label
        JLabel typeLabel = new JLabel(s);
        typeLabel.setOpaque(true);
        typeLabel.setBackground(fieldColor);
        typeLabel.setPreferredSize(new Dimension(100, ROW_HEIGHT)); // Fixed size for the type column
        typeLabel.setForeground(nameColor); // White text color
        typeLabel.setBorder(new MatteBorder(0, 0, 1, 0, borderColor)); // White border (none, none, bottom, none)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        panel.add(typeLabel, gbc);

        // "-" button
        JButton removeButton = new JButton("-");
        removeButton.setPreferredSize(new Dimension(50, ROW_HEIGHT)); // Fixed size for the button
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        removeButton.setBorder(new MatteBorder(0, 0, 1, 1, borderColor)); // White border (none, none, bottom, right)
        panel.add(removeButton, gbc);

        removeButton.setBackground(fieldColor);
        removeButton.setForeground(minusColor);
        removeButton.setFocusPainted(false);

        // Remove button action
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(panel, "Are you sure you want to remove this data field?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    dataFieldList.remove(dataField);
                    panel.remove(nameLabel);
                    panel.remove(typeLabel);
                    panel.remove(removeButton);
                    panel.revalidate();
                    panel.repaint();
                }
            }
        });
    }

    public ArrayList<Object> getDataFieldList() {
        return dataFieldList;
    }
}
