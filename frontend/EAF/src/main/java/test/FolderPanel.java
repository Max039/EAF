package test;

import compiler.ClassType;
import compiler.SyntaxTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static test.DragDropRectanglesWithSplitPane.customizeScrollBar;

public class FolderPanel extends JPanel {
    private Stack<String> pathStack;
    private JButton backButton;
    private JPanel folderDisplayPanel;
    private JScrollPane scrollPane;
    private JLabel pathLabel;  // Label for displaying the current path
    private Map<String, Set<String>> folderStructure;
    private Map<String, List<ClassType>> classTypeMap;

    public static Color pathColor = new Color(255, 255, 255);

    public FolderPanel(List<ClassType> classTypes) {
        this.folderStructure = parsePaths(classTypes);
        this.classTypeMap = mapClassTypesToPackages(classTypes);
        this.pathStack = new Stack<>();
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        setLayout(new BorderLayout());

        // Initialize the path label
        pathLabel = new JLabel("Path: Root");
        pathLabel.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        pathLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pathLabel.setForeground(pathColor);
        add(pathLabel, BorderLayout.NORTH);

        backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!pathStack.isEmpty()) {
                    pathStack.pop();
                }
                updateFolderDisplay();
                String currentPath = String.join(".", pathStack);
                updatePathLabel();
                printClassTypesBelow(currentPath);
                DragDropRectanglesWithSplitPane.subFrame.rightPanel.getVerticalScrollBar().setValue(0);
                DragDropRectanglesWithSplitPane.subFrame.rightPanel.revalidate();
                DragDropRectanglesWithSplitPane.subFrame.rightPanel.repaint();
            }
        });

        folderDisplayPanel = new JPanel();
        folderDisplayPanel.setBorder(BorderFactory.createEmptyBorder());
        folderDisplayPanel.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        folderDisplayPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
        scrollPane = new JScrollPane(folderDisplayPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(DragDropRectanglesWithSplitPane.bgColor);
        customizeScrollBar(scrollPane);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder());
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(pathLabel, BorderLayout.CENTER);
        topPanel.setBackground(DragDropRectanglesWithSplitPane.bgColor);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        String currentPath = String.join(".", pathStack);
        updateFolderDisplay();
        updatePathLabel();
        printClassTypesBelow(currentPath);
    }

    void updateFolderDisplay() {
        folderDisplayPanel.removeAll();

        String currentPath = String.join(".", pathStack);
        Set<String> currentFolders = folderStructure.getOrDefault(currentPath, new HashSet<>());

        for (String folder : currentFolders) {
            pathStack.push(folder);
            String currentPath2 = String.join(".", pathStack);
            Set<String> currentFolders2 = folderStructure.getOrDefault(currentPath2, new HashSet<>());
            pathStack.pop();

            String add = "";
            if (currentFolders2.isEmpty()) {
                add = ".dl";
            }
            JButton folderButton = new JButton(folder + add);
            folderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pathStack.push(folder);
                    updateFolderDisplay();
                    printClassTypesBelow(String.join(".", pathStack));
                    updatePathLabel();
                    DragDropRectanglesWithSplitPane.subFrame.rightPanel.getVerticalScrollBar().setValue(0);
                    DragDropRectanglesWithSplitPane.subFrame.rightPanel.revalidate();
                    DragDropRectanglesWithSplitPane.subFrame.rightPanel.repaint();
                    revalidate();
                    repaint();
                }
            });
            folderDisplayPanel.add(folderButton);
        }

        revalidate();
        repaint();
    }

    private void printClassTypesBelow(String currentPath) {
        List<ClassType> classTypes = getClassTypesInSubtree(currentPath);
        // Assuming classTypes is a List of objects with a name property
        List<ClassType> sortedClassTypes = classTypes.stream()
                .sorted(Comparator.comparing(classType -> classType.name))
                .collect(Collectors.toList());

        DragDropRectanglesWithSplitPane.subFrame.rightPanel.setRects(new ArrayList<>(sortedClassTypes));
    }

    private void updatePathLabel() {
        String currentPath = String.join(".", pathStack);
        pathLabel.setText("Path: " + (currentPath.isEmpty() ? "Root" : currentPath));
    }

    private List<ClassType> getClassTypesInSubtree(String currentPath) {
        List<ClassType> result = new ArrayList<>();
        for (Map.Entry<String, List<ClassType>> entry : classTypeMap.entrySet()) {
            if (entry.getKey().startsWith(currentPath)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    private Map<String, Set<String>> parsePaths(List<ClassType> classTypes) {
        Map<String, Set<String>> structure = new HashMap<>();
        for (ClassType classType : classTypes) {
            String path = classType.pack;
            String[] parts = path.split("\\.");
            StringBuilder currentPath = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                String key = currentPath.toString();
                structure.putIfAbsent(key, new HashSet<>());
                structure.get(key).add(parts[i]);
                if (currentPath.length() > 0) {
                    currentPath.append(".");
                }
                currentPath.append(parts[i]);
            }
        }
        return structure;
    }

    private Map<String, List<ClassType>> mapClassTypesToPackages(List<ClassType> classTypes) {
        return classTypes.stream().collect(Collectors.groupingBy(ct -> ct.pack));
    }
}
