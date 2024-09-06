package eaf.ui.panels;

import eaf.manager.ColorManager;
import eaf.manager.LogManager;
import eaf.Main;
import eaf.models.Pair;
import eaf.rects.OptionsFieldRect;
import eaf.rects.Rect;
import eaf.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ErrorPane extends JScrollPane {
    private static JPanel contentPanel;
    private static JPanel errorListPanel;
    private static JPanel warningListPanel;

    public static HashMap<Rect, Pair<Integer, String>> erroRects = new HashMap<>();
    public static HashMap<Rect, Pair<Integer, String>> warningRects = new HashMap<>();

    public static int errors = 0;
    public static int warnings = 0;
    public static int first = 0;

    public static int spacingX = 10;
    public static int spacingY = 5;

    public static Color bg = Main.bgColor;

    public static Color header = new Color(255, 255, 255);

    public ErrorPane() {
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder());

        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(DataFieldListPane.fieldColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  // Slight padding to keep content from touching the edges

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);  // Removed padding around components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Ensure components fill horizontally
        gbc.gridx = 0;

        // Create labels for errors and warnings
        JLabel errorLabel = new JLabel("Errors:");
        errorLabel.setForeground(DataFieldListPane.headColor);
        JLabel warningLabel = new JLabel("Warnings:");
        warningLabel.setForeground(header);

        // Create panels for the error and warning lists
        errorListPanel = new JPanel();
        errorListPanel.setBackground(bg);
        errorListPanel.setBorder(BorderFactory.createEmptyBorder());
        errorListPanel.setLayout(new BoxLayout(errorListPanel, BoxLayout.Y_AXIS));

        warningListPanel = new JPanel();
        warningListPanel.setBackground(bg);
        warningListPanel.setBorder(BorderFactory.createEmptyBorder());
        warningListPanel.setLayout(new BoxLayout(warningListPanel, BoxLayout.Y_AXIS));

        // Create separators
        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        separator1.setForeground(Color.WHITE);  // Set color to white
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(Color.WHITE);  // Set color to white
        JSeparator separator3 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(Color.WHITE);  // Set color to white
        JSeparator separator4 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(Color.WHITE);  // Set color to white

        // Add components to contentPanel
        gbc.gridy = 0;
        contentPanel.add(errorLabel, gbc);

        gbc.insets = new Insets(5, 0, 0, 0);  // Removed padding around components

        gbc.gridy = 1;
        contentPanel.add(separator1, gbc);

        gbc.gridy = 2;
        contentPanel.add(errorListPanel, gbc);

        gbc.insets = new Insets(10, 0, 5, 0);  // Removed padding around components
        gbc.gridy = 3;
        contentPanel.add(separator2, gbc);  // Add separator between error list and warning label

        gbc.insets = new Insets(5, 0, 0, 0);  // Removed padding around components
        gbc.gridy = 4;
        contentPanel.add(warningLabel, gbc);

        gbc.gridy = 5;
        contentPanel.add(separator3, gbc);

        gbc.gridy = 6;
        contentPanel.add(warningListPanel, gbc);

        gbc.insets = new Insets(10, 0, 5, 0);  // Removed padding around components
        gbc.gridy = 7;
        contentPanel.add(separator4, gbc);  // Optional: Add another separator if needed

        // Create containerPanel to encapsulate contentPanel
        JPanel containerPanel = new JPanel(null);  // Use null layout to control the position of contentPanel
        containerPanel.setBackground(bg);

        // Set size of containerPanel to match contentPanel's preferred size
        Dimension contentSize = contentPanel.getPreferredSize();
        contentPanel.setBounds(spacingX, spacingY, contentSize.width, contentSize.height);
        containerPanel.setPreferredSize(contentSize);
        containerPanel.add(contentPanel);

        // Set the containerPanel as the viewport view
        setViewportView(containerPanel);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }



    public static void clearErrors() {
        erroRects.clear();
        warningRects.clear();
    }

    public static void checkForErrors(Rect r) {
        r.setValidity();
        if (r instanceof RectWithRects) {
            for (var t : ((RectWithRects) r).getSubRects()) {
                if (t != null) {
                    checkForErrors(t);
                }
            }
        } else if (r instanceof OptionsFieldRect) {
            ((OptionsFieldRect) r).refreshComboBoxOptions();
        }
    }

    public static void checkForErrors() {
        Main.mainPanel.leftPanel.forceAdjustRects();
        clearErrors();

        System.out.println(LogManager.errorManager() + " Checking for errors");

        for (var r : Main.mainPanel.leftPanel.getRects()) {
            checkForErrors(r);

        }

        if (Main.preset != null && !Main.mainPanel.leftPanel.getRects().isEmpty()) {
            if (Main.mainPanel.leftPanel.getRects().size() != Main.preset.requiredRectNames.size()) {
                System.out.println("test1");
                erroRects.put(Main.mainPanel.leftPanel.getRects().get(0), new Pair<>(0, "Incorrect of \"base\" rectangles in panel for preset! The preset requires: " + concatenateArrayList(Main.preset.requiredRectNames)));
            }
            else {
                int i = 0;
                for (var r : Main.mainPanel.leftPanel.getRects()) {
                    if (!r.clazz.name.equals(Main.preset.requiredRectNames.get(i))) {
                        System.out.println("test2");
                        erroRects.put(r, new Pair<>(r.getY(), "Mismatching rectangle for preset at index " + i + " requiring " + Main.preset.requiredRectNames.get(i) + " instead of " + r.clazz.name));
                    }
                    i++;
                }
            }
        }


        updateLabels();
        warnings = warningRects.size();
        errors = erroRects.size();

        if (!erroRects.isEmpty()) {
            first = erroRects.values().stream().map(Pair::getFirst).min(Integer::compareTo).get();
        }
    }

    public static String concatenateArrayList(ArrayList<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        // Use StringBuilder for efficient string concatenation
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            if (i < list.size() - 1) {
                result.append(", ");
            }
        }

        return result.toString();
    }


    private static void updateLabels() {
        errorListPanel.removeAll();
        warningListPanel.removeAll();

        // Add warning buttons
        for (var entry : warningRects.entrySet().stream().sorted(Comparator.comparing(t -> t.getValue().getFirst())).toList()) {
            Pair<Integer, String> pair = entry.getValue();
            JButton warningButton = new JButton(pair.getSecond());
            warningButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(pair.getFirst());
                }
            });
            warningButton.setBackground(bg);
            warningButton.setForeground(ColorManager.warningColor);
            warningButton.setFocusPainted(false);
            warningButton.setBorder(BorderFactory.createEmptyBorder());
            warningListPanel.add(warningButton);
        }

        // Add error buttons
        for (var entry : erroRects.entrySet().stream().sorted(Comparator.comparing(t -> t.getValue().getFirst())).toList()) {
            Pair<Integer, String> pair = entry.getValue();
            JButton errorButton = new JButton(pair.getSecond());
            errorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(pair.getFirst());
                }
            });
            errorButton.setBackground(bg);
            errorButton.setForeground(ColorManager.errorColor);
            errorButton.setFocusPainted(false);
            errorButton.setBorder(BorderFactory.createEmptyBorder());
            errorListPanel.add(errorButton);
        }

        // Recalculate size of contentPanel
        contentPanel.revalidate();
        contentPanel.repaint();

        // Adjust the size of the containerPanel
        Dimension contentSize = contentPanel.getPreferredSize();
        contentPanel.setBounds(spacingX, spacingY, contentSize.width, contentSize.height);

        // Get the parent container panel and update its preferred size
        Container parent = contentPanel.getParent();
        if (parent instanceof JPanel) {
            JPanel containerPanel = (JPanel) parent;
            containerPanel.setPreferredSize(contentSize);
            containerPanel.revalidate();
            containerPanel.repaint();
        }
    }

}
