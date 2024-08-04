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

public class FolderPanel extends JPanel {
    private Stack<String> pathStack;
    private JButton backButton;
    private JPanel folderDisplayPanel;
    private JScrollPane scrollPane;
    private Map<String, Set<String>> folderStructure;
    private Map<String, List<ClassType>> classTypeMap;

    public FolderPanel(List<ClassType> classTypes) {
        this.folderStructure = parsePaths(classTypes);
        this.classTypeMap = mapClassTypesToPackages(classTypes);
        this.pathStack = new Stack<>();

        setLayout(new BorderLayout());

        backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!pathStack.isEmpty()) {
                    pathStack.pop();
                    updateFolderDisplay();
                }
            }
        });

        folderDisplayPanel = new JPanel();
        folderDisplayPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
        scrollPane = new JScrollPane(folderDisplayPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(backButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updateFolderDisplay();
    }

    private void updateFolderDisplay() {
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
                    printClassTypesBelow(currentPath);
                }
            });
            folderDisplayPanel.add(folderButton);
        }

        revalidate();
        repaint();
    }

    private void printClassTypesBelow(String currentPath) {
        List<ClassType> classTypes = getClassTypesInSubtree(currentPath);

        DragDropRectanglesWithSplitPane.subFrame.rightPanel.setRects(new ArrayList<>(classTypes));
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
