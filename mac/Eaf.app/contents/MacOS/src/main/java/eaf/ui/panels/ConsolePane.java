package eaf.ui.panels;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.ExtraRectManager;
import eaf.manager.LogManager;
import eaf.ui.UiUtil;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ConsolePane extends JScrollPane {
    private JTextPane textPane;
    private StyledDocument doc;

    public ConsolePane() {
        // Initialize the JTextPane with default settings
        textPane = new JTextPane();
        textPane.setEditable(false);  // Make it uneditable
        textPane.setContentType("text/plain"); // Ensure plain text
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textPane.setBackground(Main.bgColor);
        textPane.setForeground(Color.white);
        textPane.setBorder(BorderFactory.createLineBorder(Main.bgColor, 10));
        setBackground(Main.bgColor);

        // Initialize the StyledDocument
        doc = textPane.getStyledDocument();

        // Set the JTextPane as the viewport of the JScrollPane
        this.setViewportView(textPane);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        printInfo();
    }

    // Method to print text without a newline
    public void print(String text) {
        appendStyledText(text, Color.white);
    }

    // Method to print text with a newline
    public void println(String text) {
        appendStyledText(text + "\n", Color.white);
    }

    // Method to clear the text area
    public void flush() {
        textPane.setText("");
        textPane.setCaretPosition(0);
        textPane.repaint();  // Repaint the text area to update the display
    }

    public void printInfo() {
        String os = "";
        switch (Main.os) {
            case MAC -> {
                os = "Mac";
            }
            case WINDOWS -> {
                os = "Windows";
            }
        }
        println("Running on " + os);
        println("Using " + Main.programName + " version " + Main.version);
        println("Using EvoAl Version " + Main.evoalVersion);
        println("Definition Paths:");
        for (var p : SyntaxTree.pathToSyntax) {
            println(p);
        }
        println("Modules loaded " + SyntaxTree.moduleRegister.size());
        println("Classes Loaded " + SyntaxTree.getClasses().size());
        println("Extra Rects Loaded " + ExtraRectManager.classRegister.size());
        println("Constants Loaded " + SyntaxTree.constantRegister.size());
        var r = Main.cacheManager.getFirstElement(String.class, "filesOpened");
        if (r != null) {
            println("Current Project Path: " + r);
        }
    }

    // Method to print colored text
    public void printColored(String text, Color color) {
        appendStyledText(text, color);
    }

    // Helper method to append styled text
    private void appendStyledText(String text, Color color) {
        JScrollBar verticalScrollBar = getVerticalScrollBar();

        // Check if the scrollbar is at the bottom before inserting text
        boolean scrollToBottom = isScrollAtBottom();

        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        if (scrollToBottom) {
            // Ensure we scroll to the bottom after the text is added
            SwingUtilities.invokeLater(() -> {
                textPane.setCaretPosition(doc.getLength());
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            });
        }
    }

    // Helper method to check if the scroll pane is scrolled to the bottom
    private boolean isScrollAtBottom() {
        JScrollBar verticalScrollBar = getVerticalScrollBar();
        int currentPosition = verticalScrollBar.getValue() + verticalScrollBar.getVisibleAmount();
        int bottomPosition = verticalScrollBar.getMaximum();
        // Allow a small buffer to account for potential timing issues
        return currentPosition >= bottomPosition - verticalScrollBar.getUnitIncrement(1);
    }
}
