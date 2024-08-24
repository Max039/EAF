package eaf.ui;

import eaf.compiler.SyntaxTree;
import eaf.*;
import eaf.input.InputHandler;
import eaf.manager.ExtraRectManager;
import eaf.manager.FileManager;
import eaf.models.ClassType;
import eaf.models.FieldType;
import eaf.models.FieldValue;
import eaf.rects.Rect;
import eaf.rects.multi.ClassRect;
import eaf.rects.multi.RectWithRects;
import eaf.sound.SoundManager;
import eaf.ui.panels.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static eaf.Main.*;
import static eaf.manager.FileManager.loadSave;
import static eaf.manager.FileManager.readJSONFileToJSON;
import static eaf.executor.Executor.run;


public class UiUtil {

    public static final int RESIZE_MARGIN = 10; // Margin size to trigger resize
    private static Point initialClick;
    private static Dimension initialSize;
    private static Point initialLocation;


    public static BufferedImage cross = null;
    public static BufferedImage checkmark = null;

    public static int pX, pY;

    public static BufferedImage runable = null;
    public static BufferedImage unrunable = null;

    public static BufferedImage warning = null;
    public static ClassType selectedType = null;
    private static Stack<ClassType> historyStack = new Stack<>();

    static {
        try {
            // Load the image from the root directory
            cross = ImageIO.read(new File("imgs/cross.png"));

            // Load the image from the root directory
            checkmark = ImageIO.read(new File("imgs/checkmark.png"));

            // Load the image from the root directory
            runable = ImageIO.read(new File("imgs/runable.png"));

            // Load the image from the root directory
            unrunable = ImageIO.read(new File("imgs/unrunable.png"));

            warning = ImageIO.read(new File("imgs/warning.png"));


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
        fileMenu.setBackground(Main.bgColor);
        fileMenu.setForeground(Color.WHITE);
        menuBar.add(fileMenu);
    }

    static void addRectMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu fileMenu = new JMenu("Rects");


        // Create menu items
        JMenuItem newRect = new JMenuItem("new");

        newRect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClassEditor(null, false);
            }
        });


        fileMenu.add(newRect);


        JMenuItem newChild = new JMenuItem("new child");

        newChild.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClassEditor(SyntaxTree.get("empty-string-initial"), true);
            }
        });

        fileMenu.add(newChild);

        // Create menu items
        JMenuItem edit = new JMenuItem("new from existing / edit");

        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClassEditor(SyntaxTree.get("evolutionary-algorithm"), false);
            }
        });

        fileMenu.add(edit);




        JMenuItem reload = new JMenuItem("reload");

        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SyntaxTree.reload();
            }
        });


        fileMenu.add(reload);
        fileMenu.setBackground(Main.bgColor);
        fileMenu.setForeground(Color.WHITE);
        menuBar.add(fileMenu);
    }


    static void addScriptMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu scriptMenu = new JMenu("Script");

        // Create menu items
        JMenuItem run = new JMenuItem("run");

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });


        scriptMenu.add(run);
        scriptMenu.setBackground(Main.bgColor);
        scriptMenu.setForeground(Color.WHITE);
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



    public static void customizeScrollBar(JScrollPane scrollPane) {
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e){
                if (!Main.mainPanel.leftPanelTextField.hasFocus() && !Main.mainPanel.rightPanelTextField.hasFocus()) {
                    Main.mainPanel.leftPanel.requestFocus();
                }
            }
        });


        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();


        verticalScrollBar.setUI(createScrollbarUi());

        horizontalScrollBar.setUI(createScrollbarUi());


        verticalScrollBar.setUnitIncrement(16);  // Unit increment (for small scroll)
        verticalScrollBar.setBlockIncrement(100); // Block increment (for larger scroll)

        horizontalScrollBar.setUnitIncrement(16);  // Unit increment (for small scroll)
        horizontalScrollBar.setBlockIncrement(100); // Block increment (for larger scroll)

    }

    private static BasicScrollBarUI createScrollbarUi() {
        return new BasicScrollBarUI() {
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
        };
    }

    public static void adjustMainPanel(Main main) {
        main.setBorder(BorderFactory.createEmptyBorder());
        main.setLayout(new BorderLayout());
        Main.mainPanel = main;
    }

    public static void createContentLabel() {
        InputHandler.contentLabel = new JLabel("");
    }

    public static void createSplitPanes(Main main, JPanel rightContainerPanel, JPanel leftContainerPanel) {

        JSplitPane rightTopSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, Main.constantManager, main.folderPanel), 100, 0.1);

        JSplitPane rightSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTopSplitPane, rightContainerPanel), 200, 0.2);


        JSplitPane leftTopSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, Main.dataPanel, Main.errorManager), 100, 0.1);

        // Create a vertical split pane for the left side
        JSplitPane leftSplitPane = createSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTopSplitPane, leftContainerPanel), 200, 0.2);

        createMainSplitPane(main, leftSplitPane, rightSplitPane);
    }

    public static void setAdaptersAndListeners(Main main) {
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

    private static JSplitPane createSplitPane(JSplitPane VERTICAL_SPLIT, int dividerLocation, double resize) {
        // Create a vertical split pane to hold the new panel and the right container panel
        JSplitPane rightSplitPane = VERTICAL_SPLIT;
        rightSplitPane.setDividerLocation(dividerLocation); // Initial divider location
        rightSplitPane.setResizeWeight(resize); // Adjust resize weight as needed
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
                    if (matchingRect != null && (main.leftPanel.hasFocus() || main.rightPanel.hasFocus())) {
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

    public static JPanel createLeftContainerPanel(Main main) {
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
            if (ErrorPane.errors > 0) {
                var picX = r.getWidth() - cross.getWidth() - unrunable.getWidth() - RectWithRects.spacing * 2  - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
                g2.drawImage(unrunable, picX, picY, unrunable.getWidth(), unrunable.getHeight(), null);
            }
            else {
                if (ErrorPane.warnings > 0) {
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

    public static void setLeftPanelTextFieldListeners(final Main main) {
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

    public static JPanel createRightContainerPanel(Main main) {
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

    public static void setRightPanelTextFieldListeners(Main main) {
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

    public static void createFolderPanel(Main main) {
        main.folderPanel = new FolderPanel(SyntaxTree.getNonAbstractClasses());
        customizeScrollBar(main.rightPanel);
        customizeScrollBar(main.leftPanel);
    }

    public static void createErrorPanel() {
        Main.errorManager = new ErrorPane();
        customizeScrollBar(Main.errorManager);
    }

    public static void createConstantPanel() {
        Main.constantManager = new ConstantPane();
        customizeScrollBar(Main.constantManager);
    }

    public static JTextField createTextField() {
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

    public static void createDataPanel() {
        Main.dataPanel = new DataFieldListPane();
        Main.dataPanel.setBackground(Main.bgColor);
        Main.dataPanel.setBackground(Main.bgColor);
        customizeScrollBar(Main.dataPanel);
    }

    public static void createMenuBar() {
        // Create and set up the menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Main.dividerColor);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 0));
        menuBar.setForeground(Color.WHITE);


        addFileMenu(menuBar);

        addScriptMenu(menuBar);
        addRectMenu(menuBar);

        // Set the menu bar to the frame
        mainPanel.add(menuBar, BorderLayout.NORTH);

    }



    // Method to create and show the main GUI window
    public static void displayClassInfo(ClassType classType, Point p) {
        JFrame frame = new JFrame("Class Information Viewer");
        int width = 400;
        int height = 730;


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

        String back = "← Back";
        if (!historyStack.isEmpty()) {
            back += " to " + historyStack.peek().name;
        }


        // Add a back button if there is history
        JButton backButton = new JButton(back);
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


        var root = classType.getRoot();
        // Add a back button if there is history
        JButton rootButton = new JButton("Root: " + root.name);
        rootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                historyStack.push(classType);
                updateClassInfo(container, root, frame);
            }
        });
        panel.add(rootButton);

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
                        var c = SyntaxTree.get(type.typeName);
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





        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        JLabel uses = new JLabel("Uses");
        panel.add(uses);

        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space


        // Add the child names with clickable buttons inside a scroll pane
        JPanel usesPanel = new JPanel();
        usesPanel.setLayout(new BoxLayout(usesPanel, BoxLayout.Y_AXIS));

        for (var c : SyntaxTree.getClasses()) {
            for (var f : c.fields.entrySet()) {
                var type = f.getValue().getFirst();
                var class2 = SyntaxTree.get(type.typeName);
                if (!type.primitive && class2.matchesType(classType)) {
                    JButton childButton = new JButton(c.name + " - " + f.getKey() + " : " + repeatString("array ", type.arrayCount)  + type.typeName);
                    childButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Set button to full width
                    childButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    childButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            historyStack.push(classType);
                            updateClassInfo(container, c, frame);
                        }
                    });
                    usesPanel.add(childButton);
                }

            }
        }
        JScrollPane scrollPane3 = new JScrollPane(usesPanel);
        scrollPane3.setPreferredSize(new Dimension(380, 150));
        panel.add(scrollPane3);



        container.add(panel, BorderLayout.CENTER);

        // Revalidate and repaint the frame to update the UI
        frame.revalidate();
        frame.repaint();
    }

    public static String repeatString(String str, int times) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < times; i++) {
            result.append(str);
        }

        return result.toString();
    }

    public static void openClassEditor(ClassType classType, boolean newChild) {

        // Frame setup
        JFrame frame = new JFrame("Class Editor");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        frame.setSize(600, 600); // Adjust frame size if necessary
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Name TextField
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.33; // 1/3 of the width
        gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("Class Name:");
        frame.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.67; // 2/3 of the width
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Take up the remaining space
        JTextField nameField = new JTextField(40); // Adjusted size
        String name = (classType != null) ? classType.getName() : "";
        if (newChild) {
            name += "-child";
        }
        nameField.setText(name);
        frame.add(nameField, gbc);

        // Package TextField
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.33; // 1/3 of the width
        gbc.gridwidth = 1;
        JLabel packageLabel = new JLabel("Package:");
        frame.add(packageLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.67; // 2/3 of the width
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Take up the remaining space
        JTextField packageField = new JTextField(40); // Adjusted size
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
            var newP = chooseInstance(frame, null, false);
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
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setPreferredSize(new Dimension(800, 300)); // Adjust preferred size if necessary


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
            arrayField.setText("0");
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
            chooseInstance(frame, fieldsPanel, true);
        });
        frame.add(addInstanceButton, gbc);

        // Fields ScrollPane
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane fieldsScrollPane = new JScrollPane(fieldsPanel);
        fieldsScrollPane.setPreferredSize(new Dimension(500, 450)); // Set preferred size for scroll pane
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
                JTextField fieldTypeField = new JTextField(repeatString("array ", fieldType.arrayCount) + fieldType.typeName, 15);
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

    public static ClassType chooseInstance(JFrame owner, JPanel fieldsPanel, boolean addListener) {
        List<ClassType> availableClasses = SyntaxTree.getClasses();

        // Use a modal dialog to block until selection is made
        JDialog instanceDialog = new JDialog(owner, "Add Instance Field", true);
        instanceDialog.setPreferredSize(new Dimension(300, 500)); // Set preferred size for scroll pane
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
                if (!nameField.getText().isEmpty()) {
                    JPanel fieldPanel = new JPanel(new FlowLayout());
                    JTextField fieldName = new JTextField(nameField.getText(), 10);
                    fieldName.setEnabled(false);
                    fieldName.setDisabledTextColor(Color.BLACK);
                    JTextField fieldType = new JTextField(repeatString("array ", Integer.parseInt(arrayCnt.getText())) + c.name, 10);
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
                else {
                    JOptionPane.showMessageDialog(instanceFrame, "Field Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }

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



    public static JPanel getHeader() {
        // Create the main title bar panel
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(Main.mainFrame.getWidth(), 30));
        titleBar.setBackground(RectPanel.instanceColor);

        // Add a label for the title on the left with padding
        JLabel titleLabel = new JLabel(programName);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(5, 15, 5, 0)); // Top, left, bottom, right padding
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Make the button panel transparent
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0)); // Align buttons to the right with no gap

        // Load the images for the buttons
        ImageIcon minimizeIcon = new ImageIcon("imgs/minimize.png");
        ImageIcon fullscreenIcon = new ImageIcon("imgs/fullscreen.png");
        ImageIcon closeIcon = new ImageIcon("imgs/close.png");

        // Create a method to add hover effect
        MouseListener hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                button.setContentAreaFilled(true);
                button.setBackground(scrollBarButton);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                button.setContentAreaFilled(false);
                button.setBackground(Main.bgColor);
            }
        };

        // Add the minimize button
        JButton minimizeButton = new JButton(minimizeIcon);
        minimizeButton.setBackground(Main.bgColor);
        minimizeButton.setFocusPainted(false);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.addActionListener(e -> Main.mainFrame.setState(JFrame.ICONIFIED));
        minimizeButton.addMouseListener(hoverEffect);
        buttonPanel.add(minimizeButton);

        // Add the fullscreen button
        JButton fullscreenButton = new JButton(fullscreenIcon);
        fullscreenButton.setBackground(Main.bgColor);
        fullscreenButton.setFocusPainted(false);
        fullscreenButton.setBorderPainted(false);
        fullscreenButton.setContentAreaFilled(false);
        fullscreenButton.addActionListener(e -> {
            if (Main.mainFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                Main.mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                Main.mainFrame.setExtendedState(JFrame.NORMAL);
            }
        });
        fullscreenButton.addMouseListener(hoverEffect);
        buttonPanel.add(fullscreenButton);

        // Add the close button
        JButton closeButton = new JButton(closeIcon);
        closeButton.setBackground(Main.bgColor);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> InputHandler.tryClose());
        closeButton.addMouseListener(hoverEffect);
        buttonPanel.add(closeButton);

        // Add the button panel to the title bar
        titleBar.add(buttonPanel, BorderLayout.EAST);

        // Add dragging functionality to the custom title bar
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                pX = me.getX();
                pY = me.getY();
            }
        });
        titleBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                Main.mainFrame.setLocation(Main.mainFrame.getLocation().x + me.getX() - pX,
                        Main.mainFrame.getLocation().y + me.getY() - pY);
            }
        });

        return titleBar;
    }



    public static void setResizer() {
        ComponentResizer cr = new ComponentResizer();
        cr.setMinimumSize(new Dimension(300, 300));
        cr.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        cr.registerComponent(mainFrame);
        cr.setSnapSize(new Dimension(10, 10));
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
