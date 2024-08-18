package test;

import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ErrorManager extends JScrollPane {
    private static JPanel contentPanel;
    private JButton errorButton;
    private JButton warningButton;
    private static JPanel errorListPanel;
    private static JPanel warningListPanel;

    public static HashMap<Rect, Pair<Integer, String>> erroRects = new HashMap<>();
    public static HashMap<Rect, Pair<Integer, String>> warningRects = new HashMap<>();

    public static int errors = 0;
    public static int warnings = 0;
    public static int first = 0;

    public static Color header = new Color(255, 255, 255);

    public ErrorManager() {

        setBackground(Main.bgColor);
        setBorder(BorderFactory.createEmptyBorder());

        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());

        contentPanel.setBackground(Main.bgColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create buttons and panels for errors and warnings
        JLabel errorLabel = new JLabel("Errors:");
        errorLabel.setForeground(header);
        JLabel warningLabel = new JLabel("Warnings:");
        warningLabel.setForeground(header);

        errorListPanel = new JPanel();
        errorListPanel.setBackground(Main.bgColor);
        errorListPanel.setBorder(BorderFactory.createEmptyBorder());
        errorListPanel.setLayout(new BoxLayout(errorListPanel, BoxLayout.Y_AXIS));

        warningListPanel = new JPanel();
        warningListPanel.setBackground(Main.bgColor);
        warningListPanel.setBorder(BorderFactory.createEmptyBorder());
        warningListPanel.setLayout(new BoxLayout(warningListPanel, BoxLayout.Y_AXIS));

        // Add components to contentPanel
        gbc.gridy = 0;
        contentPanel.add(errorLabel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(errorListPanel, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(warningLabel, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(warningListPanel, gbc);

        setViewportView(contentPanel);
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
        updateLabels();
        warnings = warningRects.size();
        errors = erroRects.size();

        if (!erroRects.isEmpty()) {
            first = erroRects.values().stream().map(Pair::getFirst).min(Integer::compareTo).get();
        }
    }

    private static void updateLabels() {
        errorListPanel.removeAll();
        warningListPanel.removeAll();

        // Add warning buttons
        for (var entry : warningRects.entrySet()) {
            Pair<Integer, String> pair = entry.getValue();
            JButton warningButton = new JButton(pair.getSecond());
            warningButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(pair.getFirst());
                }
            });
            warningButton.setBackground(Main.bgColor);
            warningButton.setForeground(ColorManager.warningColor);
            warningButton.setFocusPainted(false);
            warningButton.setBorder(BorderFactory.createEmptyBorder());
            warningListPanel.add(warningButton);
        }

        // Add error buttons
        for (var entry : erroRects.entrySet()) {
            Pair<Integer, String> pair = entry.getValue();
            JButton errorButton = new JButton(pair.getSecond());
            errorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(pair.getFirst());
                }
            });
            errorButton.setBackground(Main.bgColor);
            errorButton.setForeground(ColorManager.errorColor);
            errorButton.setFocusPainted(false);
            errorButton.setBorder(BorderFactory.createEmptyBorder());
            errorListPanel.add(errorButton);
        }

        // Refresh the view
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
