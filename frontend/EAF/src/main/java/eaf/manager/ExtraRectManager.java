package eaf.manager;

import eaf.compiler.ClassFactory;
import eaf.compiler.SyntaxTree;
import eaf.models.ClassType;
import eaf.models.FieldType;
import eaf.models.FieldValue;
import eaf.models.Pair;
import eaf.ui.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static eaf.models.ClassType.getClassHierarchy;

public class ExtraRectManager {
    static String pathToRects = "/rects";
    static String extension = ".rect";

    public static HashMap<String, ClassType> baseClassRegister;

    public static HashMap<String, ClassType> classRegister;
    public static ClassType selectedType = null;

    public static void saveRect(ClassType c) {
        var content = ClassFactory.toRectFile(c);
        String rootDirectory = System.getProperty("user.dir") + pathToRects + "/" + c.pack.replace(".", "/") + "/" + c.name + extension;
        FileManager.writeJSONToFile(content, rootDirectory);
    }

    public static void loadRects() {
        classRegister = new HashMap<>();
        baseClassRegister  = new HashMap<>();
        // Get the root directory for .rect files
        File rootDirectory = new File(System.getProperty("user.dir") + pathToRects);

        // Recursively find and process all .rect files
        loadRectFiles(rootDirectory);
    }

    private static void loadRectFiles(File directory) {
        List<File> filesToProcess = new ArrayList<>();

        // Recursively collect all .rect files
        collectRectFiles(directory, filesToProcess);

        // Keep track of whether we processed any files in the last full iteration
        boolean processedFile;

        do {
            processedFile = false;
            Iterator<File> iterator = filesToProcess.iterator();

            while (iterator.hasNext()) {
                File file = iterator.next();
                String filePath = file.getPath();
                System.out.print("Trying to process: " + filePath);

                // Generate the package name by removing the base path and modifying the rest
                String pack = filePath
                        .replace("\\", "/")
                        .replace(System.getProperty("user.dir").replace("\\", "/") + pathToRects + "/", "")
                        .replace(extension, "")
                        .replace("/", ".");

                Pair<Boolean, ClassType> res;
                try {
                    // Create the ClassType object from the .rect file
                    res = ClassFactory.fromRectFile(FileManager.readJSONFileToJSON(file), pack);

                    // Check if the processing was successful
                    if (res.getFirst()) {

                        var c = res.getSecond();



                        if (SyntaxTree.get(c.name) != null) {
                            System.out.println(" -> processing " + ColorManager.colorText("failed", ColorManager.errorColor) + " rect file name \"" + c.name + "\" already taken!");
                        }
                        else {
                            System.out.println(" -> "  + ColorManager.colorText("successfully", ColorManager.sucessColor) +  " created \"" + c.name + "\"");
                            processedFile = true;
                            iterator.remove(); // Remove the successfully processed file from the list
                            // Register the ClassType object
                            classRegister.put(c.name, c);
                            if (c.parent == null) {
                                baseClassRegister.put(c.name, c);
                            }

                            if (c.parent != null) {
                                c.parent.addChild(c);
                            }
                        }

                    } else {
                        System.out.println(" -> processing " + ColorManager.colorText("failed", ColorManager.errorColor) + " parent not found!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } while (processedFile && !filesToProcess.isEmpty());

        System.out.println("============================");
        if (filesToProcess.isEmpty()) {
            System.out.println(ColorManager.colorText("Successfully", ColorManager.sucessColor) + " loaded all .rect files!");
        }
        else {
            for (var leftFiles : filesToProcess) {
                System.out.println(LogManager.error() + " Could not load file: " + leftFiles.getPath());
            }
        }

    }

    // Helper method to recursively collect .rect files
    private static void collectRectFiles(File directory, List<File> filesToProcess) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectRectFiles(file, filesToProcess);
            } else if (file.isFile() && file.getName().endsWith(extension)) {
                filesToProcess.add(file);
            }
        }
    }
    public static void start() {
        loadRects();
    }

    public static void openClassEditor(ClassType classType, boolean newChild) {
        // Frame setup
        JFrame frame = new JFrame("Class Editor");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Name TextField
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Class Name:");
        frame.add(nameLabel, gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        String name = (classType != null) ? classType.getName() : "";
        if (newChild) {
            name += "-child";
        }
        nameField.setText(name);
        frame.add(nameField, gbc);

        // Package TextField
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel packageLabel = new JLabel("Package:");
        frame.add(packageLabel, gbc);

        gbc.gridx = 1;
        JTextField packageField = new JTextField(20);
        packageField.setText(classType != null ? classType.pack : "");
        frame.add(packageField, gbc);

        // Abstract CheckBox
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel abstractLabel = new JLabel("Is Abstract:");
        frame.add(abstractLabel, gbc);

        gbc.gridx = 1;
        JCheckBox isAbstractCheckBox = new JCheckBox();
        isAbstractCheckBox.setSelected(classType != null && classType.isAbstract);
        frame.add(isAbstractCheckBox, gbc);

        // Parent Class ComboBox
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel parentLabel = new JLabel("Parent Class:");
        frame.add(parentLabel, gbc);

        gbc.gridx = 1;
        JButton parentButton = new JButton();
        String parent = (classType != null) ? (newChild ? classType.name : (classType.parent != null ? classType.parent.name : "")) : "";
        parentButton.setText(parent);
        parentButton.addActionListener(e -> {
            var newP = chooseInstance(null, false);
            if (newP != null) {
                parentButton.setText(newP.name);
            }
        });
        frame.add(parentButton, gbc);

        gbc.gridx = 2;
        JButton xParent = new JButton("X");
        xParent.addActionListener(ae -> parentButton.setText(""));
        frame.add(xParent, gbc);

        JPanel fieldsPanel = new JPanel();

        // Add Primitive Button
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JButton addPrimitiveButton = new JButton("Add Primitive Field");
        addPrimitiveButton.addActionListener(e -> {
            JFrame primitiveFrame = new JFrame("Add Primitive Field");
            primitiveFrame.setLayout(new GridBagLayout());
            GridBagConstraints gbcPrimitive = new GridBagConstraints();
            gbcPrimitive.insets = new Insets(5, 5, 5, 5); // Padding between components
            gbcPrimitive.anchor = GridBagConstraints.WEST;
            gbcPrimitive.fill = GridBagConstraints.HORIZONTAL;

            primitiveFrame.setSize(300, 200);
            primitiveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Field Name
            gbcPrimitive.gridx = 0;
            gbcPrimitive.gridy = 0;
            JLabel fieldNameLabel = new JLabel("Field Name:");
            primitiveFrame.add(fieldNameLabel, gbcPrimitive);

            gbcPrimitive.gridx = 1;
            JTextField fieldNameField = new JTextField(15);
            primitiveFrame.add(fieldNameField, gbcPrimitive);

            // Field Type
            gbcPrimitive.gridx = 0;
            gbcPrimitive.gridy = 1;
            JLabel fieldTypeLabel = new JLabel("Field Type:");
            primitiveFrame.add(fieldTypeLabel, gbcPrimitive);

            gbcPrimitive.gridx = 1;
            String[] types = {"quotient real", "int", "string"};
            JComboBox<String> typeComboBox = new JComboBox<>(types);
            primitiveFrame.add(typeComboBox, gbcPrimitive);

            // Array Count
            gbcPrimitive.gridx = 0;
            gbcPrimitive.gridy = 2;
            JLabel arrayCountLabel = new JLabel("Array Count:");
            primitiveFrame.add(arrayCountLabel, gbcPrimitive);

            gbcPrimitive.gridx = 1;
            JTextField arrayField = new JTextField(15);
            primitiveFrame.add(arrayField, gbcPrimitive);

            // Value
            gbcPrimitive.gridx = 0;
            gbcPrimitive.gridy = 3;
            JLabel valueLabel = new JLabel("Value:");
            primitiveFrame.add(valueLabel, gbcPrimitive);

            gbcPrimitive.gridx = 1;
            JTextField fieldValueField = new JTextField(15);
            primitiveFrame.add(fieldValueField, gbcPrimitive);


            // OK Button
            gbcPrimitive.gridx = 0;
            gbcPrimitive.gridy = 4;
            gbcPrimitive.gridwidth = 2;
            gbcPrimitive.fill = GridBagConstraints.HORIZONTAL;
            JButton okButton = new JButton("OK");
            primitiveFrame.add(okButton, gbcPrimitive);

            okButton.addActionListener(ae -> {
                // Get the input values
                String fieldName = fieldNameField.getText().trim();
                String fieldValue = fieldValueField.getText().trim();
                String arrayCountText = arrayField.getText().trim();

                // Validate the inputs
                if (fieldName.isEmpty()) {
                    JOptionPane.showMessageDialog(primitiveFrame, "Field Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int arrayCount = Integer.parseInt(arrayCountText);
                    if (arrayCount > 0 && !fieldValue.isEmpty()) {
                        JOptionPane.showMessageDialog(primitiveFrame, "Value field must be empty if Array Count is greater than 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(primitiveFrame, "Array Count must be a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // If validation passes, proceed to add the field
                JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField fieldNameFieldDisplay = new JTextField(fieldName, 15);
                JTextField fieldTypeField = new JTextField((!arrayCountText.isEmpty() ? "array " + arrayCountText + " " : "") + typeComboBox.getSelectedItem(), 15);
                JCheckBox isPrimitive = new JCheckBox("Primitive", true);
                JTextField fieldValueFieldDisplay = new JTextField(fieldValue, 15);
                JButton removeFieldButton = new JButton("-");

                fieldPanel.add(new JLabel("Field:"));
                fieldPanel.add(fieldNameFieldDisplay);
                fieldPanel.add(fieldTypeField);
                fieldPanel.add(isPrimitive);
                fieldPanel.add(fieldValueFieldDisplay);
                fieldPanel.add(removeFieldButton);

                removeFieldButton.addActionListener(e1 -> fieldsPanel.remove(fieldPanel));

                fieldsPanel.add(fieldPanel);
                fieldsPanel.revalidate();
                primitiveFrame.dispose();
            });

            primitiveFrame.setVisible(true);
        });
        frame.add(addPrimitiveButton, gbc);

        // Add Instance Button
        gbc.gridx = 2;
        gbc.gridy = 4;
        JButton addInstanceButton = new JButton("Add Instance Field");
        addInstanceButton.addActionListener(e -> {
            chooseInstance(fieldsPanel, true);
        });
        frame.add(addInstanceButton, gbc);


        // Fields ScrollPane
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        JScrollPane fieldsScrollPane = new JScrollPane(fieldsPanel);
        frame.add(fieldsScrollPane, gbc);

        // Adding existing fields
        if (classType != null) {
            for (var entry : classType.fields.entrySet()) {
                String fieldName = entry.getKey();
                FieldType fieldType = entry.getValue().getFirst();
                FieldValue fieldValue = entry.getValue().getSecond();

                JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField fieldNameField = new JTextField(fieldName, 15);
                fieldNameField.setEnabled(false);
                fieldNameField.setDisabledTextColor(Color.BLACK);
                JTextField fieldTypeField = new JTextField(UiUtil.repeatString("array ", fieldType.arrayCount) + fieldType.typeName, 15);
                fieldTypeField.setEnabled(false);
                fieldTypeField.setDisabledTextColor(Color.BLACK);
                JCheckBox isPrimitiveCheckBox = new JCheckBox("Primitive", fieldType.primitive);
                isPrimitiveCheckBox.setEnabled(false);
                JTextField fieldValueField = new JTextField(fieldValue != null ? fieldValue.value : "", 15);
                fieldValueField.setEnabled(false);
                fieldValueField.setDisabledTextColor(Color.BLACK);
                JButton removeFieldButton = new JButton("-");

                fieldPanel.add(new JLabel("Field:"));
                fieldPanel.add(fieldNameField);
                fieldPanel.add(fieldTypeField);
                fieldPanel.add(isPrimitiveCheckBox);
                fieldPanel.add(fieldValueField);
                fieldPanel.add(removeFieldButton);

                removeFieldButton.addActionListener(e -> fieldsPanel.remove(fieldPanel));

                fieldsPanel.add(fieldPanel);
            }
        }



        // Confirm Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            ClassType p = null;
            if (!parentButton.getText().isEmpty()) {
                p = SyntaxTree.get((String) parentButton.getText());
            }

            ClassType newClassType = new ClassType(nameField.getText(), p, packageField.getText());
            newClassType.setAbstract(isAbstractCheckBox.isSelected());

            for (Component comp : fieldsPanel.getComponents()) {
                if (comp instanceof JPanel fieldPanel) {
                    JTextField fieldNameField = (JTextField) fieldPanel.getComponent(1);
                    JTextField fieldTypeField = (JTextField) fieldPanel.getComponent(2);
                    JCheckBox isPrimitiveCheckBox = (JCheckBox) fieldPanel.getComponent(3);
                    JTextField fieldValueField = (JTextField) fieldPanel.getComponent(4);

                    var typeParts = fieldTypeField.getText().split("array ");
                    FieldType fieldType = new FieldType(typeParts[typeParts.length - 1], isPrimitiveCheckBox.isSelected(), typeParts.length - 1);
                    newClassType.addField(fieldNameField.getText(), fieldType);
                    if (isPrimitiveCheckBox.isSelected()) {
                        if (!fieldValueField.getText().isEmpty()) {
                            newClassType.setField(fieldNameField.getText(), new FieldValue(fieldType.typeName, fieldValueField.getText()), false);
                        }
                    }
                }
            }
            ExtraRectManager.saveRect(newClassType);
            SyntaxTree.reload();
            frame.dispose();
        });
        frame.add(confirmButton, gbc);

        frame.setVisible(true);
    }



    public static ClassType chooseInstance(JPanel fieldsPanel, boolean addListener) {
        List<ClassType> availableClasses = SyntaxTree.getClasses();

        // Use a modal dialog to block until selection is made
        JDialog instanceDialog = new JDialog((Frame) null, "Add Instance Field", true);
        instanceDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ensure text fields stretch horizontally

        // Set size and default close operation
        instanceDialog.setSize(300, 400);
        instanceDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Name Field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        instanceDialog.add(nameLabel, gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        nameField.setPreferredSize(new Dimension(150, nameField.getPreferredSize().height));
        instanceDialog.add(nameField, gbc);

        // Array Count Field
        JTextField arrayField = new JTextField(15);
        if (addListener) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            JLabel arrayCountLabel = new JLabel("Array Count:");
            instanceDialog.add(arrayCountLabel, gbc);

            gbc.gridx = 1;
            arrayField.setText("0");
            arrayField.setPreferredSize(new Dimension(150, arrayField.getPreferredSize().height));
            instanceDialog.add(arrayField, gbc);
        }

        // Search Field with Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel searchLabel = new JLabel("Search:");
        instanceDialog.add(searchLabel, gbc);

        gbc.gridx = 1;
        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, searchField.getPreferredSize().height));
        instanceDialog.add(searchField, gbc);

        // Instance List Panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JPanel instanceListPanel = new JPanel();
        instanceListPanel.setLayout(new BoxLayout(instanceListPanel, BoxLayout.Y_AXIS));

        for (ClassType availableClass : availableClasses) {
            JButton classButton = new JButton(availableClass.getName());
            if (addListener) {
                classButton.addActionListener(addListener(nameField, availableClass, arrayField, instanceDialog, fieldsPanel));
            } else {
                classButton.addActionListener(terminator(instanceDialog, availableClass));
            }
            instanceListPanel.add(classButton);
        }


        JScrollPane instanceScrollPane = new JScrollPane(instanceListPanel);
        instanceDialog.add(instanceScrollPane, gbc);


        instanceDialog.pack();
        instanceDialog.setLocationRelativeTo(null);  // Center the dialog
        instanceDialog.setVisible(true);  // This will block until the dialog is closed

        return selectedType;  // Return the selected class after the dialog is closed
    }





    public static ActionListener addListener(JTextField nameField, ClassType c, JTextField arrayCnt, JDialog instanceFrame, JPanel fieldsPanel) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel fieldPanel = new JPanel(new FlowLayout());
                JTextField fieldName = new JTextField(nameField.getText(), 10);
                fieldName.setEnabled(false);
                fieldName.setDisabledTextColor(Color.BLACK);
                JTextField fieldType = new JTextField(UiUtil.repeatString("array ", Integer.parseInt(arrayCnt.getText())) + c.name, 10);
                fieldType.setEnabled(false);
                fieldType.setDisabledTextColor(Color.BLACK);
                JCheckBox isPrimitive = new JCheckBox("Primitive", false);
                isPrimitive.setEnabled(false);
                JTextField fieldValue = new JTextField("", 10);
                fieldValue.setEnabled(false);
                fieldValue.setDisabledTextColor(Color.BLACK);
                JButton removeFieldButton = new JButton("-");

                fieldPanel.add(new JLabel("Field:"));
                fieldPanel.add(fieldName);
                fieldPanel.add(fieldType);
                fieldPanel.add(isPrimitive);
                fieldPanel.add(fieldValue);
                fieldPanel.add(removeFieldButton);

                removeFieldButton.addActionListener(e1 -> fieldsPanel.remove(fieldPanel));

                selectedType = c;

                fieldsPanel.add(fieldPanel);
                fieldsPanel.revalidate();
                instanceFrame.dispose();
            }
        };
    }

    public static ActionListener terminator(JDialog instanceFrame, ClassType c) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedType = c;
                instanceFrame.dispose();
            }
        };
    }
}
