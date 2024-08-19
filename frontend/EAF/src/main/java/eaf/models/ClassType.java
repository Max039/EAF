package eaf.models;

import eaf.compiler.SyntaxTree;
import eaf.manager.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClassType implements Comparable {

    public HashMap<String, Pair<FieldType, FieldValue>> fields;

    public String getName() {
        return name;
    }

    public String name;

    String spacing = " ";

    public boolean isAbstract = false;

    private boolean extending;

    ClassType parent;

    ArrayList<ClassType> children = new ArrayList<>();

    public String pack;

    private static Stack<ClassType> historyStack = new Stack<>();

    public ClassType(String name, ClassType parent, String pack) {
        this.name = name;
        this.parent = parent;
        this.pack = pack;
        fields = new HashMap<>();
        if ( parent != null ) {
            fields.putAll(parent.fields);
            extending = true;
        }
        else {
            extending = false;
        }
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public void setExtending(boolean extending) {
        this.extending = extending;
    }

    public void addChild(ClassType child) {
        children.add(child);
    }

    public boolean matchesType(ClassType c) {
        if (this.equals(c)) {
            return true;
        }
        for (var child : children) {
            if (child.matchesType(c)) {
                return true;
            }
        }
        return false;
    }

    boolean doesExtend() {
        return parent != null;
    }

    Pair<FieldType, FieldValue> getFieldPair(String name) {
        return fields.get(name);
    }

    public void addField(String name, FieldType type) {
        var currVal = fields.get(name);
        if (currVal != null && currVal.getSecond() != null) {
            throw new SyntaxTree.FieldAlreadyDefinedException("When trying to define field \"" + name + "\" for type \"" + name + "\" field name was already defined!");
        }
        fields.put(name, new Pair<>(type, null));
    }

    public void setField(String name, FieldValue v) {
        var currVal = fields.get(name);
        if (currVal == null) {
            fields.put(name, new Pair<>(v.type, v));
        }
        else {
            System.out.println(LogManager.field() + "Trying to set field \"" + name + "\" of type \"" + currVal.getFirst() + "\" with current value \"" + currVal.getSecond() + "\" to \"" + v + "\"" );
            boolean typesMatch;
            if (currVal.getFirst().primitive) {
                typesMatch = currVal.getFirst().equals(v.type);
            }
            else {
                typesMatch = FieldValue.doesTypesMatch(currVal.getFirst(), v.type);
            }
            if (typesMatch && currVal.getFirst().primitive == v.type.primitive && currVal.getFirst().arrayCount == v.type.arrayCount) {
                fields.put(name, new Pair<>(v.type, v));
            }
            else {
                throw new SyntaxTree.FieldTypeMismatchException("Tried to add array element to type but types are not compatible \n\"" + currVal.getFirst().toString() + "\" != \n\"" + v.type.toString() + "\"");
            }
        }

    }

    public String getParent() {
        return parent == null ? "null" : parent.toString();
    }

    public String getParentName() {
        return parent == null ? "null" : parent.name;
    }

    public String getChildrenNames() {
        String s = "";
        for (var child : children) {
            if (s.isEmpty()) {
                s = child.name + " [" + child.getChildrenNames() + "]";
            }
            else {
                s += ", " + child.name + " [" + child.getChildrenNames() + "]";
            }
        }
        return s;
    }

    // Method to return the class hierarchy tree as a string in the specified format
    public static String getClassHierarchy(ClassType root, String indent, boolean last, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        if (isRoot) {
            sb.append("─── ");
            indent += "    ";
        } else {
            if (last) {
                sb.append("└── ");
                indent += "    ";
            } else {
                sb.append("├── ");
                indent += "│   ";
            }
        }
        var childrenSorted = root.children.stream().sorted().toList();
        sb.append(root.name + " (" + "\u001B[37m" + root.pack  + "\u001B[0m" + ")").append("\n");
        for (int i = 0; i < childrenSorted.size(); i++) {
            sb.append(getClassHierarchy(childrenSorted.get(i), indent, i == childrenSorted.size() - 1, false));
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(spacing + "Class = " + name + " Parent: " + getParentName() + " Children: [" + getChildrenNames() + "]" +  " {\n" + spacing + " Fields: [\n");
        for (var t : fields.entrySet()) {
            String v = "";
            if (t.getValue().getSecond() != null) {
                v = t.getValue().getSecond().toString();
            }

            s.append(spacing).append("Field = ").append(t.getKey()).append(" : ").append(v).append("\n");
        }
        s.append(spacing).append(" ]\n" + spacing + "}");
        return s.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassType classType = (ClassType) o;
        return Objects.equals(pack, classType.pack) && Objects.equals(name, classType.name) && isAbstract == classType.isAbstract && extending == classType.extending;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || getClass() != o.getClass())  {
            return -1;
        }
        else {
            ClassType c = (ClassType) o;
            int res =  c.name.compareTo(name);
            if (res > 0) {
                res = -1;
            }
            else if (res < 0) {
                res = 1;
            }
            return res;

        }
    }

    public static String getUniqueImports(ArrayList<ClassType> classTypes) {
        Set<String> packages = new HashSet<>();
        for (ClassType classType : classTypes) {
            collectPackages(classType, packages);
        }

        List<String> sortedPackages = packages.stream().sorted().collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (String pack : sortedPackages) {
            sb.append("import \"definitions\" from ").append(pack).append(";\n");
        }
        return sb.toString();
    }

    // Helper method to collect packages from the class and its parents
    private static void collectPackages(ClassType classType, Set<String> packages) {
        if (classType != null) {
            packages.add(classType.pack);
            collectPackages(classType.parent, packages);
        }
    }

    public ClassType instance() {
        var c = new ClassType(name, parent, pack);
        c.setAbstract(isAbstract);
        c.children = children;
        for (var v : fields.entrySet()) {
            var pair = v.getValue();
            FieldValue val = null;
            if (pair.getSecond() != null) {
                val = pair.getSecond().clone();
            }
            c.fields.put(v.getKey(), new Pair<>(pair.getFirst(), val));
        }

        return c;
    }

    public List<ClassType> getAllClassTypes() {
        List<ClassType> result = new ArrayList<>();
        result.add(this);
        for (ClassType child : children) {
            result.addAll(child.getAllClassTypes());
        }
        return result;
    }


    // Method to create and show the main GUI window
    public static void displayClassInfo(ClassType classType, Point p) {
        JFrame frame = new JFrame("Class Information Viewer");
        int width = 400;
        int height = 500;


        // Create the initial content
        updateClassInfo(frame.getContentPane(), classType, frame);
        frame.setBounds(p.x - width/2, p.y - height/2, width, height);
        frame.setVisible(true);
    }

    // Method to update the contents of the window with the class information
    // Method to update the contents of the window with the class information
    public static void updateClassInfo(Container container, ClassType classType, JFrame frame) {
        container.removeAll();
        container.setLayout(new BorderLayout());

        // Panel to hold all the components with vertical layout and spacing
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Adding 15px spacing between components
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add a back button if there is history
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!historyStack.isEmpty()) {
                    ClassType previousClass = historyStack.pop();
                    updateClassInfo(container, previousClass, frame);
                }
            }
        });
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space


        // Add the class name
        JLabel nameLabel = new JLabel("Class Name: " + classType.getName());
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        // Add the package name
        JLabel packageLabel = new JLabel("Package: " + classType.pack);
        panel.add(packageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        JLabel fields = new JLabel("Fields");
        panel.add(fields);

        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        // Add the child names with clickable buttons inside a scroll pane
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        for (var field : classType.fields.entrySet()) {
            var type = field.getValue().getFirst();

            String value = "";
            if (field.getValue().getSecond() != null && field.getValue().getFirst().primitive) {
                value = " = " +field.getValue().getSecond().value;
            }

            JButton fieldButton = new JButton(field.getKey() + " : " + repeatString("array ", type.arrayCount)  + type.typeName + value);
            fieldButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Set button to full width
            fieldButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            fieldButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!type.primitive) {
                        var c = SyntaxTree.classRegister.get(type.typeName);
                        historyStack.push(classType);
                        updateClassInfo(container, c, frame);
                    }
                }
            });
            fieldButton.setEnabled(!type.primitive);
            fieldPanel.add(fieldButton);
        }

        JScrollPane scrollPane2 = new JScrollPane(fieldPanel);
        scrollPane2.setPreferredSize(new Dimension(380, 150));
        panel.add(scrollPane2);

        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        // Add the parent name with a clickable button
        JButton parentButton = new JButton("Parent: " + classType.getParentName());
        parentButton.setEnabled(classType.parent != null);
        parentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classType.parent != null) {
                    historyStack.push(classType);
                    updateClassInfo(container, classType.parent, frame);
                }
            }
        });
        panel.add(parentButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        JLabel children = new JLabel("Children");
        panel.add(children);

        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space



        // Add the child names with clickable buttons inside a scroll pane
        JPanel childrenPanel = new JPanel();
        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        for (ClassType child : classType.children) {
            JButton childButton = new JButton(child.getName());
            childButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Set button to full width
            childButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            childButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    historyStack.push(classType);
                    updateClassInfo(container, child, frame);
                }
            });
            childrenPanel.add(childButton);
        }

        JScrollPane scrollPane = new JScrollPane(childrenPanel);
        scrollPane.setPreferredSize(new Dimension(380, 150));
        panel.add(scrollPane);





        container.add(panel, BorderLayout.CENTER);

        // Revalidate and repaint the frame to update the UI
        frame.revalidate();
        frame.repaint();
    }

    public ClassType findSingleNonAbstractClass() {
        ClassType result = isAbstract ? null : this; // Set result to this class if it's non-abstract, else null
        for (ClassType child : children) {
            ClassType childResult = child.findSingleNonAbstractClass(); // Recursively check each child
            if (childResult != null) {
                if (result != null) {
                    return null; // If more than one non-abstract class is found, return null
                }
                result = childResult; // Otherwise, set result to the non-abstract child
            }
        }
        return result; // Return the single non-abstract class, or null if there's more than one
    }

    public static String repeatString(String str, int times) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < times; i++) {
            result.append(str);
        }

        return result.toString();
    }

}
