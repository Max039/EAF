package test;

import compiler.SyntaxTree;
import test.rects.Rect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static test.FileManager.loadSave;
import static test.FileManager.readJSONFileToJSON;
import static test.InputHandler.run;


public class GuiCreator {

    public static BufferedImage cross = null;
    public static BufferedImage checkmark = null;

    public static BufferedImage runable = null;
    public static BufferedImage unrunable = null;

    public static BufferedImage warning = null;

    static {
        try {
            // Load the image from the root directory
            cross = ImageIO.read(new File("cross.png"));

            // Load the image from the root directory
            checkmark = ImageIO.read(new File("checkmark.png"));

            // Load the image from the root directory
            runable = ImageIO.read(new File("runable.png"));

            // Load the image from the root directory
            unrunable = ImageIO.read(new File("unrunable.png"));

            warning = ImageIO.read(new File("warning.png"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addFileMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu fileMenu = new JMenu("File");

        // Create menu items
        JMenuItem openFileDotDotDot = new JMenuItem("open ...");

        openFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    var cancle = showUnsaveDialog();
                    if (!cancle) {
                        var file = FileManager.chooseJavaFile(Main.savesPath, Main.saveFormat);
                        if (file != null) {
                            loadSave(readJSONFileToJSON(file));
                            Main.cacheManager.addToBuffer("filesOpened", file.getPath());
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });




        JMenuItem saveFileDotDotDot = new JMenuItem("save as ...");

        saveFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.saveAs();
            }
        });



        // Create a JMenuItem that will have a submenu
        JMenuItem open = new JMenu("open recent");

        // Add a MenuListener to update the submenu before it is shown
        ((JMenu) open).addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                // Clear the submenu before repopulating
                open.removeAll();

                // Get the latest elements from the ArrayList
                ArrayList<String> items = Main.cacheManager.getBuffer(String.class, "filesOpened").getElements();

                // Populate the submenu with the updated items
                for (String item : items) {
                    JMenuItem menuItem = new JMenuItem(item);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (new File(item).exists()) {
                                    var cancle = showUnsaveDialog();
                                    if (!cancle) {
                                        loadSave(readJSONFileToJSON(item));
                                        Main.cacheManager.addToBuffer("filesOpened", item);
                                    }
                                }
                                else {
                                    JOptionPane.showMessageDialog(null, "File was not found!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    open.add(menuItem);
                }
                // Refresh the submenu (optional, depending on your use case)
                open.revalidate();
                open.repaint();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // Optional: Handle the event when the menu is deselected
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // Optional: Handle the event when the menu is canceled
            }
        });

        JMenuItem save = new JMenuItem("save");

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.save();
            }
        });



        JMenuItem newSave = new JMenuItem("new");

        newSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var cancle = showUnsaveDialog();
                if (!cancle) {
                    FileManager.newFile();
                }
            }
        });

        // Add the submenu to the main menu
        fileMenu.add(newSave);
        fileMenu.add(open);
        fileMenu.add(openFileDotDotDot);
        fileMenu.add(saveFileDotDotDot);
        fileMenu.add(save);
        menuBar.add(fileMenu);
    }


    static void addScriptMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu scriptMenu = new JMenu("File");

        // Create menu items
        JMenuItem run = new JMenuItem("run");

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });


        scriptMenu.add(run);
        menuBar.add(scriptMenu);
    }



    public static boolean showUnsaveDialog() {

        if (!InputHandler.actionHandler.areChangesMadeSinceSave()) {
            return false;
        }
        else {
            SoundManager.playExclamationSound();
            // Options for the dialog
            String[] options = {"Yes", "No", "Cancel"};

            // Show the dialog and store the selected option
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "The current file is not saved, do you wish to save?",
                    "Unsaved File",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[2]
            );

            // Handle the choice and return the corresponding value
            switch (choice) {
                case JOptionPane.YES_OPTION:
                    FileManager.save();
                    return false;
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION: // Also handle if the dialog is closed
                    return true;
                default:
                    return false;
            }
        }
    }



    static void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Main.scrollBarButton;
                this.trackColor = Main.scrollBarBg;
                this.thumbDarkShadowColor = Main.scrollBarBg;
                this.thumbHighlightColor = Main.scrollBarButton;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createArrowButton(orientation);
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createArrowButton(orientation);
            }

            private JButton createArrowButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(16, 16));
                button.setBackground(Main.scrollBarTopAndBottom);
                button.setForeground(Main.scrollBarButton);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setFocusPainted(false);
                return button;
            }
        });

        horizontalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Main.scrollBarButton;
                this.trackColor = Main.scrollBarBg;
                this.thumbDarkShadowColor = Main.scrollBarBg;
                this.thumbHighlightColor = Main.scrollBarButton;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createArrowButton(orientation);
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createArrowButton(orientation);
            }

            private JButton createArrowButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(16, 16));
                button.setBackground(Main.scrollBarTopAndBottom);
                button.setForeground(Main.scrollBarButton);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setFocusPainted(false);
                return button;
            }
        });


        verticalScrollBar.setUnitIncrement(16);  // Unit increment (for small scroll)
        verticalScrollBar.setBlockIncrement(100); // Block increment (for larger scroll)

        horizontalScrollBar.setUnitIncrement(16);  // Unit increment (for small scroll)
        horizontalScrollBar.setBlockIncrement(100); // Block increment (for larger scroll)

    }

    static void adjustMainPanel(Main main) {
        main.setBorder(BorderFactory.createEmptyBorder());
        main.setLayout(new BorderLayout());
        Main.mainPanel = main;
    }

    static void createContentLabel() {
        InputHandler.contentLabel = new JLabel("");
    }

    static void createSplitPanes(Main main, JPanel rightContainerPanel, JPanel leftContainerPanel) {
        JSplitPane rightSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, main.folderPanel, rightContainerPanel));

        // Create a vertical split pane for the left side
        JSplitPane leftSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, Main.dataPanel, leftContainerPanel));

        createMainSplitPane(main, leftSplitPane, rightSplitPane);
    }

    static void setAdaptersAndListeners(Main main) {
        MouseAdapter focus = getFocusAdapter(main.rightPanel);

        MouseAdapter mouseAdapter = rightPanelAdapter(main.rightPanel);

        MouseAdapter mouseAdapter2 = draggingAdapter(main);

        addMouseAdapters(main, focus, mouseAdapter, mouseAdapter2);

        addKeyListeners(main);
    }

    private static void createMainSplitPane(Main main, JSplitPane leftSplitPane, JSplitPane rightSplitPane) {
        main.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, rightSplitPane);
        main.mainSplitPane.setDividerLocation(400); // Initial divider location
        main.mainSplitPane.setResizeWeight(0.5); // Evenly split the panels
        main.mainSplitPane.setBackground(Main.dividerColor);
        main.mainSplitPane.setForeground(Main.dividerColor);
        main.mainSplitPane.setUI(new CustomSplitPaneUI());
        Border border3 = BorderFactory.createLineBorder(Main.dividerColor, 1);
        main.mainSplitPane.setBorder(border3);

        main.add(main.mainSplitPane, BorderLayout.CENTER);
    }

    private static JSplitPane createSplitPane(JSplitPane VERTICAL_SPLIT) {
        // Create a vertical split pane to hold the new panel and the right container panel
        JSplitPane rightSplitPane = VERTICAL_SPLIT;
        rightSplitPane.setDividerLocation(100); // Initial divider location
        rightSplitPane.setResizeWeight(0.1); // Adjust resize weight as needed
        rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
        rightSplitPane.setUI(new CustomSplitPaneUI());
        rightSplitPane.setBackground(Main.bgColor);
        return rightSplitPane;
    }

    private static void addMouseAdapters(Main main, MouseAdapter focus, MouseAdapter mouseAdapter, MouseAdapter mouseAdapter2) {
        Main.dataPanel.getViewport().getView().addMouseListener(focus);
        Main.dataPanel.getViewport().getView().addMouseMotionListener(focus);

        main.folderPanel.addMouseListener(focus);
        main.folderPanel.addMouseMotionListener(focus);

        main.folderPanel.folderDisplayPanel.addMouseListener(focus);
        main.folderPanel.folderDisplayPanel.addMouseMotionListener(focus);


        main.rightPanel.getViewport().getView().addMouseListener(mouseAdapter);
        main.rightPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
        main.leftPanel.getViewport().getView().addMouseListener(mouseAdapter2);
        main.leftPanel.getViewport().getView().addMouseMotionListener(mouseAdapter2);
    }

    private static void addKeyListeners(Main main) {
        InputHandler.addKeyListener(main.leftPanel);
        InputHandler.addKeyListener(main.rightPanel);
        InputHandler.addKeyListener(Main.dataPanel);
        InputHandler.addKeyListener(main.folderPanel.scrollPane);
    }

    private static MouseAdapter draggingAdapter(final Main main) {
        MouseAdapter mouseAdapter2 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), main.leftPanel.getViewport().getView());

                // Convert the point from the source component's coordinate system to the left panel's coordinate system
                Point panelRelativePos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Main.mainFrame);
                var prevSelected = InputHandler.selected;
                InputHandler.unselect();
                Rect matchingRect = main.leftPanel.getRect(leftPanelPos);
                if (matchingRect instanceof RectWithRects) {
                    matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                    if (matchingRect != null && main.leftPanel.hasFocus()) {
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e);
                        if (InputHandler.isControlPressed && matchingRect instanceof ClassRect && prevSelected != matchingRect) {
                            InputHandler.setSelected((ClassRect)matchingRect);
                        }
                    }
                }
                if (matchingRect == null) {
                    main.leftPanel.requestFocusInWindow();
                }
                main.repaint();

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                InputHandler.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                InputHandler.mouseReleased(e);
            }


        };
        return mouseAdapter2;
    }

    private static MouseAdapter rightPanelAdapter(final RectPanel rightPanel) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                rightPanel.requestFocusInWindow();

                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());

                Rect rect = rightPanel.getRect(point);

                if (rect != null && rect.contains(point)) {
                    InputHandler.setDraggingRect(rect.clone(), e, new Point(point.x - rect.getX(), point.y - rect.getY()), null);
                }

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                InputHandler.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                InputHandler.mouseReleased(e);
            }


        };
    }

    private static MouseAdapter getFocusAdapter(final RectPanel rightPanel) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                rightPanel.requestFocusInWindow();

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                InputHandler.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                InputHandler.mouseReleased(e);
            }


        };
    }

    static JPanel createLeftContainerPanel(Main main) {
        InputHandler.contentLabel2 = new JLabel("");
        InputHandler.contentLabel2.setOpaque(true);
        InputHandler.contentLabel2.setBackground(Main.searchBar);
        InputHandler.contentLabel2.setForeground(Main.searchBarText);

        var filterLabel2 = new JLabel("Search");
        filterLabel2.setOpaque(true);
        filterLabel2.setBackground(Main.searchBar);
        filterLabel2.setForeground(Main.searchBarText);

        // Use GridBagLayout to arrange the components
        JPanel panel2 = new JPanel(new GridBagLayout());
        panel2.setBackground(Main.searchBar); // Set background for the panel
        GridBagConstraints gbc2 = new GridBagConstraints();

        // Place the "Filter:" label on the left
        gbc2.gridx = 0; // Column 0
        gbc2.gridy = 0; // Row 0
        gbc2.insets = new Insets(0, 5, 0, 5); // Add some padding
        gbc2.anchor = GridBagConstraints.WEST; // Align to the left
        panel2.add(filterLabel2, gbc2);

        // Place the search bar in the middle
        gbc2.gridx = 1; // Column 1
        gbc2.fill = GridBagConstraints.HORIZONTAL; // Make the search bar fill the space
        gbc2.weightx = 1.0; // Allow the search bar to grow horizontally
        panel2.add(main.leftPanelTextField, gbc2);

        // Place the content label on the right
        gbc2.gridx = 2; // Column 2
        gbc2.fill = GridBagConstraints.NONE; // Do not stretch the label
        gbc2.weightx = 0.0; // Do not allow the label to grow
        panel2.add(InputHandler.contentLabel2, gbc2);

        panel2.setBackground(Main.searchBar);

        // Add the panel to the top of the container
        JPanel leftContainerPanel = new JPanel(new BorderLayout());
        leftContainerPanel.setBackground(Main.searchBar);
        panel2.setBorder(BorderFactory.createLineBorder(Main.searchBarBorder, 1));
        leftContainerPanel.add(panel2, BorderLayout.NORTH);

        leftContainerPanel.add(main.leftPanel, BorderLayout.CENTER);
        leftContainerPanel.setBorder(BorderFactory.createEmptyBorder());
        return leftContainerPanel;
    }

    public static void drawIcons(RectPanel r, Graphics g) {
            var g2 = (Graphics2D) g;
            if (InputHandler.actionHandler.areChangesMadeSinceSave()) {
                var picX = r.getWidth() - cross.getWidth() - RectWithRects.spacing - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
                g2.drawImage(cross, picX, picY, cross.getWidth(), cross.getHeight(), null);
            }
            else {
                var picX = r.getWidth() - checkmark.getWidth() - RectWithRects.spacing - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
                g2.drawImage(checkmark, picX, picY, checkmark.getWidth(), checkmark.getHeight(), null);
            }
            if (ErrorManager.errors > 0) {
                var picX = r.getWidth() - cross.getWidth() - unrunable.getWidth() - RectWithRects.spacing * 2  - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
                g2.drawImage(unrunable, picX, picY, unrunable.getWidth(), unrunable.getHeight(), null);
            }
            else {
                if (ErrorManager.warnings > 0) {
                    var picX = r.getWidth() - checkmark.getWidth() - warning.getWidth() - RectWithRects.spacing * 2 - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                    var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();;
                    g2.drawImage(warning, picX, picY, warning.getWidth(), warning.getHeight(), null);
                }
                else {
                    var picX = r.getWidth() - checkmark.getWidth() - runable.getWidth() - RectWithRects.spacing * 2 - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                    var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();;
                    g2.drawImage(runable, picX, picY, runable.getWidth(), runable.getHeight(), null);
                }

            }


    }

    static void setLeftPanelTextFieldListeners(final Main main) {
        main.leftPanelTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                InputHandler.searchChanged();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    InputHandler.current = 0;
                    var res = InputHandler.getStringMarkers();
                    int value = main.leftPanel.getVerticalScrollBar().getValue();
                    int maximum = main.leftPanel.getVerticalScrollBar().getMaximum();
                    int visibleAmount = main.leftPanel.getVerticalScrollBar().getVisibleAmount();

                    // Determine if the scrollbar is scrolled fully down
                    boolean isScrolledFullyDown = (value + visibleAmount >= maximum);

                    if (!res.isEmpty() && isScrolledFullyDown) {
                        main.leftPanel.getVerticalScrollBar().setValue(res.get(0));
                        InputHandler.current = 1;
                    }
                    else {
                        int i = main.leftPanel.getVerticalScrollBar().getValue();
                        boolean found = false;

                        for (var y : res) {
                            InputHandler.current++;
                            if (y > i) {
                                i = y;
                                found = true;
                                break;
                            }
                        }
                        if (!found && !res.isEmpty()) {
                            i = res.get(0);
                        }
                        main.leftPanel.getVerticalScrollBar().setValue(i);
                    }

                }
                InputHandler.searchChanged();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                InputHandler.searchChanged();
            }
        });

        main.leftPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InputHandler.searchChanged();
            }
        });

        main.leftPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                var res = InputHandler.getStringMarkers();
                if (!res.isEmpty() && !main.leftPanelTextField.getText().isEmpty()) {
                    main.leftPanel.getVerticalScrollBar().setValue(res.get(0));
                    InputHandler.current = 1;
                }
                if (res.isEmpty() && !main.leftPanelTextField.getText().isEmpty()) {
                    InputHandler.current = 0;
                }
                InputHandler.searchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                InputHandler.searchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                InputHandler.searchChanged();
            }
        });
    }

    static JPanel createRightContainerPanel(Main main) {
        JLabel filterLabel = new JLabel("Filter:");
        // Create a panel to hold the text field and the right panel
        main.rightContainerPanel = new JPanel();
        main.rightContainerPanel.setLayout(new BorderLayout());


        // Set background and foreground colors for the labels
        filterLabel.setOpaque(true);
        filterLabel.setBackground(Main.searchBar);
        filterLabel.setForeground(Main.searchBarText);

        InputHandler.contentLabel.setOpaque(true);
        InputHandler.contentLabel.setBackground(Main.searchBar);
        InputHandler.contentLabel.setForeground(Main.searchBarText);

        // Use GridBagLayout to arrange the components
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Main.searchBar); // Set background for the panel
        GridBagConstraints gbc = new GridBagConstraints();

        // Place the "Filter:" label on the left
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.insets = new Insets(0, 5, 0, 5); // Add some padding
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        panel.add(filterLabel, gbc);

        // Place the search bar in the middle
        gbc.gridx = 1; // Column 1
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make the search bar fill the space
        gbc.weightx = 1.0; // Allow the search bar to grow horizontally
        panel.add(main.rightPanelTextField, gbc);

        // Place the content label on the right
        gbc.gridx = 2; // Column 2
        gbc.fill = GridBagConstraints.NONE; // Do not stretch the label
        gbc.weightx = 0.0; // Do not allow the label to grow
        panel.add(InputHandler.contentLabel, gbc);

        panel.setBackground(Main.searchBar);

        // Add the panel to the top of the container
        JPanel rightContainerPanel = new JPanel(new BorderLayout());
        rightContainerPanel.setBackground(Main.searchBar);
        panel.setBorder(BorderFactory.createLineBorder(Main.searchBarBorder, 1));
        rightContainerPanel.add(panel, BorderLayout.NORTH);

        main.rightPanel.setBackground(Main.searchBar);
        rightContainerPanel.add(main.rightPanel, BorderLayout.CENTER);
        rightContainerPanel.setBorder(BorderFactory.createEmptyBorder());

        return rightContainerPanel;
    }

    static void setRightPanelTextFieldListeners(Main main) {
        main.rightPanelTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                InputHandler.filterChanged();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                InputHandler.filterChanged();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                InputHandler.filterChanged();
            }
        });

        main.rightPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InputHandler.filterChanged();
            }
        });

        main.rightPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                InputHandler.filterChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                InputHandler.filterChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                InputHandler.filterChanged();
            }
        });
    }

    static void createFolderPanel(Main main) {
        main.folderPanel = new FolderPanel(SyntaxTree.getNonAbstractClasses());
        customizeScrollBar(main.rightPanel);
        customizeScrollBar(main.leftPanel);
    }

    static JTextField createTextField() {
        var textField = new JTextField();
        textField.setBackground(Main.searchBar);
        textField.setForeground(Main.searchBarText);
        textField.setCaretColor(Main.searchBarText);
        textField.setSelectionColor(Main.searchBarText);
        textField.setSelectedTextColor(Main.searchBar);
        Border border2 = BorderFactory.createEmptyBorder();
        textField.setBorder(border2);
        return textField;
    }

    static void createDataPanel() {
        Main.dataPanel = new DataFieldListPane();
        Main.dataPanel.setBackground(Main.bgColor);
        Main.dataPanel.setBackground(Main.bgColor);
        customizeScrollBar(Main.dataPanel);
    }

    static void createMenuBar() {
        // Create and set up the menu bar
        JMenuBar menuBar = new JMenuBar();


        addFileMenu(menuBar);

        addScriptMenu(menuBar);


        // Set the menu bar to the frame
        Main.mainFrame.setJMenuBar(menuBar);
    }

    // Custom SplitPaneUI
    static class CustomSplitPaneUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new CustomSplitPaneDivider(this);
        }
    }

    // Custom SplitPaneDivider
    static class CustomSplitPaneDivider extends BasicSplitPaneDivider {
        public CustomSplitPaneDivider(BasicSplitPaneUI ui) {
            super(ui);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public void paint(Graphics g) {
            // Set the color for the divider
            g.setColor(Main.dividerColor);
            // Fill the divider with the chosen color
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }


}
