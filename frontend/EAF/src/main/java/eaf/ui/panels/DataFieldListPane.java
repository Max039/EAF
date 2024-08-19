package eaf.ui.panels;

import eaf.action.AddedDataAction;
import eaf.action.RemovedDataAction;
import eaf.models.ClassType;
import eaf.compiler.SyntaxTree;
import eaf.sound.SoundManager;
import org.json.JSONArray;
import org.json.JSONObject;
import eaf.models.DataField;
import eaf.input.InputHandler;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static eaf.models.ClassType.getUniqueImports;

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

    public HashMap<DataField, ArrayList<Component>> componentMap = new HashMap<>();

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

        JMenuItem newItem = new JMenuItem("Create");
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
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(300, 100);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        dialog.add(nameLabel, gbc);

        JTextField nameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        dialog.add(nameField, gbc);

        JButton okButton = new JButton("OK");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(okButton, gbc);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                if (!name.isEmpty()) {
                    if (isDuplicateName(name)) {
                        SoundManager.playExclamationSound();
                        JOptionPane.showMessageDialog(dialog, "Name already used!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        userAddedDataField(new DataField(name, "quotient real", false));
                        dialog.dispose();
                    }
                } else {
                    SoundManager.playExclamationSound();
                    JOptionPane.showMessageDialog(dialog, "Name must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openCreateInstanceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Instance", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(300, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        dialog.add(nameLabel, gbc);

        JTextField nameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        dialog.add(nameField, gbc);

        JLabel typeLabel = new JLabel("Type:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        dialog.add(typeLabel, gbc);

        JTextField typeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        dialog.add(typeField, gbc);

        JButton okButton = new JButton("OK");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(okButton, gbc);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String type = typeField.getText();
                if (!name.isEmpty() && !type.isEmpty()) {
                    if (isDuplicateName(name)) {
                        SoundManager.playExclamationSound();
                        JOptionPane.showMessageDialog(dialog, "Name already used!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (isInstanceNameValid(type)) {
                        userAddedDataField(new DataField(name, type, true));
                        dialog.dispose();
                    } else {
                        SoundManager.playExclamationSound();
                        String closestMatch = findClosestMatch(type);
                        int response = JOptionPane.showConfirmDialog(dialog,
                                "Instance not found. Did you mean \"" + closestMatch + "\"?",
                                "Instance Not Found", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            userAddedDataField(new DataField(name, closestMatch, true));
                            dialog.dispose();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Both fields must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAddDataFieldDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Data Field", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(300, 180);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        dialog.add(nameLabel, gbc);

        JTextField nameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        dialog.add(nameField, gbc);

        JLabel typeLabel = new JLabel("Type:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        dialog.add(typeLabel, gbc);

        JTextField typeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        dialog.add(typeField, gbc);

        JLabel instanceLabel = new JLabel("Instance:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        dialog.add(instanceLabel, gbc);

        JCheckBox instanceCheckBox = new JCheckBox();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        dialog.add(instanceCheckBox, gbc);

        JButton okButton = new JButton("OK");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(okButton, gbc);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String type = typeField.getText();
                boolean instance = instanceCheckBox.isSelected();

                if (!type.isEmpty() && !name.isEmpty()) {
                    if (isDuplicateName(name)) {
                        SoundManager.playExclamationSound();
                        JOptionPane.showMessageDialog(dialog, "Name already used!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (instance && !isInstanceNameValid(type)) {
                        SoundManager.playExclamationSound();
                        String closestMatch = findClosestMatch(type);
                        int response = JOptionPane.showConfirmDialog(dialog,
                                "Instance not found. Did you mean \"" + closestMatch + "\"?",
                                "Instance Not Found", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            userAddedDataField(new DataField(name, closestMatch, true));
                            dialog.dispose();
                        }
                    } else {
                        userAddedDataField(new DataField(name, type, instance));
                        dialog.dispose(); // Only close if the instance is valid or the name isn't a duplicate
                    }
                } else {
                    SoundManager.playExclamationSound();
                    JOptionPane.showMessageDialog(dialog, "Both fields must be filled in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private boolean isDuplicateName(String name) {
        return dataFieldList.stream()
                .anyMatch(df -> df instanceof DataField && ((DataField) df).getName().equals(name));
    }

    private boolean isInstanceNameValid(String name) {
        return SyntaxTree.getNonAbstractClasses().stream()
                .anyMatch(t -> t.getName().matches(name));
    }

    private String findClosestMatch(String name) {
        Optional<String> closestMatch = SyntaxTree.getNonAbstractClasses().stream()
                .map(t -> t.getName())
                .min((s1, s2) -> Integer.compare(getLevenshteinDistance(s1, name), getLevenshteinDistance(s2, name)));
        return closestMatch.orElse(name); // Return the original name if no matches found
    }

    private int getLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length() + 1;
        int len2 = s2.length() + 1;

        // Create the distance matrix
        int[] cost = new int[len1];
        int[] newCost = new int[len1];

        for (int i = 0; i < len1; i++) cost[i] = i;

        // Calculate the cost
        for (int j = 1; j < len2; j++) {
            newCost[0] = j;

            for (int i = 1; i < len1; i++) {
                int match = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                int costReplace = cost[i - 1] + match;
                int costInsert = cost[i] + 1;
                int costDelete = newCost[i - 1] + 1;

                newCost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
            }

            int[] swap = cost;
            cost = newCost;
            newCost = swap;
        }

        return cost[len1 - 1];
    }

    private void userAddedDataField(DataField dataField) {
        addDataField(dataField);
        InputHandler.actionHandler.action(new AddedDataAction(dataField));
    }

    public void addDataField(DataField dataField) {
        dataFieldList.add(dataField);
        addDataFieldComponent(dataField);
        ErrorPane.checkForErrors();
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

        var arr = new ArrayList<Component>();
        arr.add(nameLabel);
        arr.add(typeLabel);
        arr.add(removeButton);
        componentMap.put(dataField, arr);

        // Remove button action
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.playExclamationSound();
                int response = JOptionPane.showConfirmDialog(panel, "Are you sure you want to remove this data field?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    userRemoveDataField(dataField);

                }
            }
        });
    }

    private void userRemoveDataField(DataField dataField) {
        removeDataField(dataField);
        InputHandler.actionHandler.action(new RemovedDataAction(dataField));
    }

    public void removeDataField(DataField f) {
        var res = componentMap.get(f);
        dataFieldList.remove(f);
        for (var r : res) {
            panel.remove(r);
        }
        ErrorPane.checkForErrors();
        panel.revalidate();
        panel.repaint();
    }

    public void clearDataField() {
        dataFieldList.clear();
        componentMap.clear();
        panel.removeAll();
    }

    public ArrayList<Object> getDataFieldList() {
        return dataFieldList;
    }


    public JSONArray toJson() {
        JSONArray rts = new JSONArray();
        for (var d : dataFieldList) {
            rts.put(((DataField)d).toJson());
        }
        return rts;
    }

    public void fromJson(JSONArray arr) {
        // Remove all data fields
        clearDataField();

        // Re-add the header
        createHeader();

        // Add new data fields from JSON array
        for (var o : arr) {
            addDataField(new DataField((JSONObject)o));
        }

        // Refresh the UI
        panel.revalidate();
        panel.repaint();
    }


    public String toString() {
        String res = "";
        String data = "";
        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();

        for (var d : dataFieldList) {
            var c = ((DataField)d);
            if (c.isInstance()) {
                classesNeededForScript.add(SyntaxTree.classRegister.get(c.getType()));
            }
            data += "\t" + c.toFormat() + "\n";
        }

        res += getUniqueImports(classesNeededForScript) + "\n";
        res += "module 'config' {\n";
        res += data;
        res += "}";

        return res;
    }

}


