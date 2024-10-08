package eaf.ui;

import eaf.compiler.SyntaxTree;
import eaf.*;
import eaf.download.Downloader;
import eaf.executor.Executor;
import eaf.executor.OpenIntelliJProject;
import eaf.input.InputHandler;
import eaf.manager.ExtraRectManager;
import eaf.manager.FileManager;
import eaf.models.ClassType;
import eaf.models.FieldType;
import eaf.models.FieldValue;
import eaf.models.Pair;
import eaf.plugin.PluginCreator;
import eaf.rects.Rect;
import eaf.rects.RectFactory;
import eaf.rects.multi.ClassRect;
import eaf.rects.multi.RectWithRects;
import eaf.setup.Preset;
import eaf.sound.SoundManager;
import eaf.ui.panels.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static eaf.Main.*;
import static eaf.manager.FileManager.*;
import static eaf.plugin.PluginCreator.createNewFromExample;


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


    public static AtomicReference<ClassType> result;

    public static AtomicBoolean terminates;
    public static Object selectedObject;

    public static boolean obectSelected;

    private static Stack<ClassType> historyStack = new Stack<>();

    private static int classEntryCounterForRectMenu = 0;

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
        fileMenu.setBackground(Color.WHITE);
        fileMenu.setBackground(dividerColor);
        // Create menu items
        JMenuItem openFileDotDotDot = new JMenuItem("open ...");
        openFileDotDotDot.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        openFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    var cancle = showUnsaveDialog();
                    if (!cancle) {
                        var file = FileManager.chooseJavaFile(Main.savesPath, ".eaf", Main.saveFormat);
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


        JMenuItem imp = new JMenuItem("import ...");
        imp.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        imp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    var cancle = showUnsaveDialog();
                    if (!cancle) {
                        var file = FileManager.chooseJavaFile("/" + evoalBuildFolder + "/" + evoalVersion + "/evoal/examples", ".ddl " + Main.saveFormat + " .generator .mll .ol", Main.saveFormat, "ddl", "ol", "generator", "mll");
                        if (file != null) {
                            tryImport(file);
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });



        JMenuItem saveFileDotDotDot = new JMenuItem("save as ...");
        saveFileDotDotDot.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate

        saveFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.saveAs();
            }
        });



        // Create a JMenuItem that will have a submenu
        JMenuItem open = new JMenu("open recent");
        open.setBackground(Color.WHITE);
        open.setBackground(dividerColor);
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
                    menuItem.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
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
        save.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.save();
            }
        });



        JMenuItem newSave = new JMenuItem("new");
        newSave.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var cancle = showUnsaveDialog();
                if (!cancle) {
                    FileManager.newFile();
                }
            }
        });

        JMenuItem newSaveFromPreset = new JMenuItem("new from preset");
        newSaveFromPreset.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newSaveFromPreset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fromPreset();
            }
        });

        JMenuItem newGenerator = new JMenuItem("add generator to project");
        newGenerator.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newGenerator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGeneratorFile();
            }
        });


        // Add the submenu to the main menu
        fileMenu.add(newSave);
        fileMenu.add(newSaveFromPreset);
        fileMenu.add(open);
        fileMenu.add(openFileDotDotDot);
        fileMenu.add(saveFileDotDotDot);
        fileMenu.add(save);
        fileMenu.add(imp);
        fileMenu.setBackground(Main.bgColor);
        fileMenu.setForeground(Color.WHITE);
        fileMenu.add(newGenerator);
        menuBar.add(fileMenu);
    }

    static void addRectMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu fileMenu = new JMenu("Rects");
        fileMenu.setBackground(Color.WHITE);
        fileMenu.setBackground(dividerColor);

        // Create menu items
        JMenuItem newRect = new JMenuItem("new");
        newRect.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newRect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClassEditor(null, false, true, true, false);
            }
        });


        fileMenu.add(newRect);


        JMenuItem newChild = new JMenuItem("new child");
        newChild.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newChild.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var res = chooseInstance(mainFrame, null, false);
                if (res != null) {
                    openClassEditor(res, true, true, true, false);
                }
            }
        });

        fileMenu.add(newChild);

        // Create menu items
        JMenuItem edit = new JMenuItem("new from existing / edit");
        edit.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var res = chooseInstance(mainFrame, null, false);
                if (res != null) {
                    openClassEditor(res, false, true, true, false);
                }
            }
        });

        fileMenu.add(edit);




        JMenuItem reload = new JMenuItem("reload");
        reload.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
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
        scriptMenu.setBackground(Color.WHITE);
        scriptMenu.setBackground(dividerColor);
        // Create menu items
        JMenuItem run = new JMenuItem("run");
        run.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread executionThread = new Thread(() -> {
                    InputHandler.tryRun();
                });
                executionThread.start();
            }
        });


        scriptMenu.add(run);
        scriptMenu.setBackground(Main.bgColor);
        scriptMenu.setForeground(Color.WHITE);
        menuBar.add(scriptMenu);
    }

    static void addEvoAlMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu scriptMenu = new JMenu("EvoAl");
        scriptMenu.setBackground(Color.WHITE);
        scriptMenu.setBackground(dividerColor);
        // Create menu items
        JMenuItem run = new JMenuItem("select versions");
        run.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String currentPath = System.getProperty("user.dir");
                openFolderWindow(currentPath + "/" + evoalBuildFolder);
            }
        });


        scriptMenu.add(run);

        JMenuItem downloadMenu = new JMenuItem("manage versions");
        run.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate

        downloadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while (!isIndex()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                Downloader.showMenu();
            }
        });


        scriptMenu.add(downloadMenu);

        scriptMenu.setBackground(Main.bgColor);
        scriptMenu.setForeground(Color.WHITE);
        menuBar.add(scriptMenu);
    }

    static void addPresetMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu scriptMenu = new JMenu("Preset");
        scriptMenu.setBackground(Color.WHITE);
        scriptMenu.setBackground(dividerColor);
        // Create menu items
        JMenuItem run = new JMenuItem("change preset");
        run.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPresetWindow();
            }
        });


        scriptMenu.add(run);


        scriptMenu.setBackground(Main.bgColor);
        scriptMenu.setForeground(Color.WHITE);
        menuBar.add(scriptMenu);
    }

    static void addPluginMenu(JMenuBar menuBar) {
        // Create the File menu
        JMenu scriptMenu = new JMenu("Plugins");
        scriptMenu.setBackground(Color.WHITE);
        scriptMenu.setBackground(dividerColor);
        // Create menu items
        JMenuItem run = new JMenuItem("reload");
        run.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pluginManager.discoverPlugins();
                SyntaxTree.reload();
            }
        });


        scriptMenu.add(run);


        // Create menu items
        JMenuItem newClass = new JMenuItem("new implementation");
        newClass.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        newClass.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread executionThread = new Thread(() -> {
                    try {
                        createNewFromExample();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                executionThread.start();
            }
        });


        scriptMenu.add(newClass);

        // Create menu items
        JMenuItem openInIntellij = new JMenuItem("open in intellij");
        openInIntellij.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        openInIntellij.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenIntelliJProject.openProject(PluginCreator.currentPlugin.path);
            }
        });


        scriptMenu.add(openInIntellij);

        // Create menu items
        JMenuItem updatePom = new JMenuItem("update pom");
        updatePom.setUI(new CustomMenuItemUI(bgColor)); // Set custom UI delegate
        updatePom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PluginCreator.updateCompilerArgs();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        scriptMenu.add(updatePom);

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

        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();

                // Only scroll the vertical scrollbar
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    int rotation = e.getWheelRotation();
                    int units = e.getScrollAmount();
                    int increment = verticalBar.getUnitIncrement() * units * rotation;
                    verticalBar.setValue(verticalBar.getValue() + increment);
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

        createConsoleAndTabs(main);
    }

    private static void createConsoleAndTabs(Main main) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Create a list of pairs (tab name, tab component)
        tabsList = new ArrayList<>();

        // Add some example tabs
        tabsList.add(new Pair<>("Editor", createMainTab(Main.mainPanel)));  // Pass 'main' to createMainTab
        var a = new JPanel();
        a.add(new JLabel("Not yet supported :/"), BorderLayout.CENTER);
        tabsList.add(new Pair<>("Analysis", a));

        createTabs(screenSize);
        mergeTabAndTabs();
        createConsole();
        createConsoleSplitpane(screenSize);

        main.add(consoleSplitpane, BorderLayout.CENTER);
    }

    private static void mergeTabAndTabs() {
        // Create a new panel to hold both tabsPanel and tab
        tabAndTabs = new JPanel(new BorderLayout());

        // Ensure that tabsPanel and tab are not null
        if (tabsPanel != null) {
            tabAndTabs.add(tabsPanel, BorderLayout.NORTH);
        }
        if (tab != null) {
            tabAndTabs.add(tab, BorderLayout.CENTER);
        }

        tabAndTabs.setBorder(BorderFactory.createEmptyBorder());
    }



    private static void createTabs(Dimension screenSize) {
        // Initialize tabsPanel
        tabsPanel = new TabsPane(new Dimension(screenSize.width, 20));


        // Set layout to null to manually control component positioning
        tabsPanel.setLayout(null);

        Color selected = new Color(37, 37, 37);
        Color unselected = RectPanel.instanceColor;

        tab = tabsList.get(0).getSecond();
        tabButtons = new ArrayList<>();
        int startX = 40;
        int startY = 0;
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonSpacing = 4;

        int xAcc = startX;

        for (int i = 0; i < tabsList.size(); i++) {
            Pair<String, JComponent> tabPair = tabsList.get(i);
            String tabName = tabPair.getFirst();
            JComponent tabComponent = tabPair.getSecond();

            JButton tabButton = new JButton(tabName);
            tabButton.setForeground(Color.WHITE);
            tabButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (var button : tabButtons) {
                        button.setBackground(unselected);
                    }
                    tabButton.setBackground(selected);

                    tabAndTabs.remove(tab);
                    tab = tabComponent;
                    tabAndTabs.add(tab, BorderLayout.CENTER);
                    tabAndTabs.revalidate();
                    tabAndTabs.repaint();
                }
            });

            if (i == 0) {
                tabButton.setBackground(selected);
            }
            else {
                tabButton.setBackground(unselected);
            }
            tabButtons.add(tabButton);

            // Set the location and size manually
            tabButton.setLocation(xAcc, startY);
            tabButton.setSize(buttonWidth, buttonHeight);
            tabButton.setFocusPainted(false);
            tabButton.setBorder(BorderFactory.createEmptyBorder());
            xAcc += buttonWidth + buttonSpacing;
            tabsPanel.add(tabButton);
        }
    }



    public static JPanel createMainTab(Main main) {
        // Create the tab panel
        var tab = new JPanel(new BorderLayout());
        tab.add(main.mainSplitPane, BorderLayout.CENTER);  // Center allows it to expand
        tab.setBorder(BorderFactory.createEmptyBorder());
        return tab;
    }

    private static void createConsoleSplitpane(Dimension screenSize) {
        // Create the split pane and add tabAndTabs and console
        consoleSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabAndTabs, console);
        consoleSplitpane.setBorder(BorderFactory.createEmptyBorder());
        consoleSplitpane.setPreferredSize(screenSize);  // Ensure it fills the available space
        consoleSplitpane.setDividerLocation(450); // Initial divider location
        consoleSplitpane.setResizeWeight(0.9); // Evenly split the panels
        consoleSplitpane.setUI(new CustomSplitPaneUI());
    }

    private static void createConsole() {
        // Create the console panel
        console = new ConsolePane();
        customizeScrollBar(console);
        console.setBorder(BorderFactory.createLineBorder(RectPanel.instanceColor, 10));
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
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e, true);
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
                Point panelRelativePos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Main.mainFrame);

                Rect rect = rightPanel.getRect(point);

                if (e.getButton() == 1 && rect != null && rect.contains(point)) {
                    InputHandler.setDraggingRect(rect.clone(), e, new Point(point.x - rect.getX(), point.y - rect.getY()), null);
                }


                if (e.getButton() == 3 && rect instanceof RectWithRects) {
                    rect = ((RectWithRects) rect).getSubRect(point);
                    if (rect != null) {
                        rect.onMouseClicked(e.getButton() == 1, point, panelRelativePos, e, false);
                    }
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
            r.rightButton.setSize(cross.getWidth(), cross.getHeight());
            r.rightButton.setLocation(picX, picY);
            g2.drawImage(cross, picX, picY, cross.getWidth(), cross.getHeight(), null);
        }
        else {
            var picX = r.getWidth() - checkmark.getWidth() - RectWithRects.spacing - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
            var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
            r.rightButton.setSize(checkmark.getWidth(), checkmark.getHeight());
            r.rightButton.setLocation(picX, picY);
            g2.drawImage(checkmark, picX, picY, checkmark.getWidth(), checkmark.getHeight(), null);
        }
        if (ErrorPane.errors > 0 || preset == null || mainPanel.leftPanel.getRects().isEmpty()) {
            var picX = r.getWidth() - cross.getWidth() - unrunable.getWidth() - RectWithRects.spacing * 2  - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
            var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();
            r.leftButton.setSize(unrunable.getWidth(), unrunable.getHeight());
            r.leftButton.setLocation(picX, picY);
            g2.drawImage(unrunable, picX, picY, unrunable.getWidth(), unrunable.getHeight(), null);
        }
        else {
            if (ErrorPane.warnings > 0) {
                var picX = r.getWidth() - checkmark.getWidth() - warning.getWidth() - RectWithRects.spacing * 2 - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();;
                r.leftButton.setSize(warning.getWidth(), warning.getHeight());
                r.leftButton.setLocation(picX, picY);
                g2.drawImage(warning, picX, picY, warning.getWidth(), warning.getHeight(), null);
            }
            else {
                var picX = r.getWidth() - checkmark.getWidth() - runable.getWidth() - RectWithRects.spacing * 2 - Main.mainPanel.leftPanel.getVerticalScrollBar().getWidth();
                var picY = r.getVerticalScrollBar().getValue() + RectWithRects.spacing + Main.mainPanel.leftPanel.getHorizontalScrollBar().getHeight();;
                r.leftButton.setSize(runable.getWidth(), runable.getHeight());
                r.leftButton.setLocation(picX, picY);
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
        menuBar.setUI(new CustomMenuBarUI()); // Set the custom UI delegate

        addFileMenu(menuBar);

        addScriptMenu(menuBar);
        addRectMenu(menuBar);
        //addPluginMenu(menuBar);
        addEvoAlMenu(menuBar);
        addPresetMenu(menuBar);

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
            back += " to " + SyntaxTree.toSimpleName(historyStack.peek().name);
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
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.setEnabled(!historyStack.isEmpty());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space


        // Add the class name
        JTextArea nameLabel = new JTextArea("Class Name: " + SyntaxTree.toSimpleName(classType.getName()));
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space
        // Get the font from the JLabel
        Font labelFont = new JLabel().getFont();

        // Set the same font to the JTextArea
        nameLabel.setFont(labelFont);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        var root = classType.getRoot();
        // Add a back button if there is history
        JButton rootButton = new JButton("Root: " + SyntaxTree.toSimpleName(root.name));
        rootButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                historyStack.push(classType);
                updateClassInfo(container, root, frame);
            }
        });
        if (root.equals(classType)) {
            rootButton.setEnabled(false);
            rootButton.setText("Root: this");
        }
        panel.add(rootButton);

        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space

        // Add the package name
        JTextArea packageLabel = new JTextArea("Package: " + classType.pack);
        panel.add(packageLabel);
        packageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Add vertical space
        packageLabel.setFont(labelFont);

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

            String name = type.typeName;
            if (!type.primitive) {
                name = SyntaxTree.toSimpleName(name);
            }

            JButton fieldButton = new JButton(field.getKey() + " : " + repeatString("array ", type.arrayCount)  + name + value);
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

        String parent = "";
        if (classType.parent != null) {
            parent = SyntaxTree.toSimpleName(classType.getParentName());
        }

        // Add the parent name with a clickable button
        JButton parentButton = new JButton("Parent: " + parent);
        parentButton.setAlignmentX(Component.LEFT_ALIGNMENT);
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
            JButton childButton = new JButton(SyntaxTree.toSimpleName(child.getName()));
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
                if (!type.primitive) {
                    var class2 = SyntaxTree.get(type.typeName);
                    if (class2.matchesType(classType)) {
                        JButton childButton = new JButton(SyntaxTree.toSimpleName(c.name)+ " - " + f.getKey() + " : " + repeatString("array ", type.arrayCount)  + SyntaxTree.toSimpleName(type.typeName));
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

    public static void openClassEditor(ClassType classType, boolean newChild, boolean saveAsRect, boolean parentChangeable, boolean forceNonAbstract) {

        classEntryCounterForRectMenu = 0;

        // Frame setup
        JFrame frame = new JFrame("Class Editor");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        frame.setSize(1200, 1000); // Adjust frame size if necessary
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
        if (forceNonAbstract) {
            isAbstractCheckBox.setEnabled(true);
        }
        else {
            isAbstractCheckBox.setSelected(classType != null && classType.isAbstract);
        }
        frame.add(isAbstractCheckBox, gbc);

        // Parent Class ComboBox
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel parentLabel = new JLabel("Parent Class:");
        frame.add(parentLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
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

        if (parentChangeable) {
            gbc.gridx = 2;
            JButton xParent = new JButton("X");
            xParent.addActionListener(ae -> parentButton.setText(""));
            frame.add(xParent, gbc);
        }
        else {
            parentButton.setEnabled(false);
        }


        JPanel fieldsPanel = new JPanel();
        //fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
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
            String[] types = {"quotient real", "int", "string", "data"};
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
                    if (typeComboBox.getSelectedItem().equals("data") && !fieldValue.isEmpty()) {
                        JOptionPane.showMessageDialog(primitiveFrame, "Value field must be empty if data type is selected!.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(primitiveFrame, "Array Count must be a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                rectEditorEntryPanel(fieldName, repeatString("array ", Integer.parseInt(arrayCountText)) + typeComboBox.getSelectedItem(), true, fieldValue, fieldsPanel);
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
        JScrollPane fieldsScrollPane = new JScrollPane(fieldsPanel) {
            @Override
            public Dimension getPreferredSize() {
                int maxX = 100;
                int maxY = 0;
                for (var comp : getComponents()) {
                    maxX = Math.max(maxX, comp.getX() + comp.getWidth());
                    maxY = Math.max(maxY, comp.getY() + comp.getHeight());
                }
                return new Dimension(maxX, maxY);
            };
        };
        fieldsScrollPane.setSize(100, 0);

        frame.add(fieldsScrollPane, gbc);

        // Adding existing fields
        if (classType != null) {
            for (var entry : classType.fields.entrySet()) {
                String fieldName = entry.getKey();
                FieldType fieldType = entry.getValue().getFirst();
                FieldValue fieldValue = entry.getValue().getSecond();

                rectEditorEntryPanel(fieldName, repeatString("array ", fieldType.arrayCount) + fieldType.typeName, entry.getValue().getFirst().primitive, fieldValue != null ? fieldValue.value : "", fieldsPanel);
            }
        }

        // Confirm Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton confirmButton = new JButton("Confirm");

        result = new AtomicReference<>(null);
        terminates = new AtomicBoolean(false);

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
            if (saveAsRect) {
                ExtraRectManager.saveRect(newClassType);
                SyntaxTree.reload();
            }
            result.set(newClassType);
            frame.dispose();
            terminates.set(true);
        });
        frame.add(confirmButton, gbc);

        frame.setVisible(true);


    }

    public static ClassType openClassEditorAndReturn(ClassType classType, boolean newChild, boolean saveAsRect) {
        openClassEditor(classType, newChild, saveAsRect, false, true);

        while (!terminates.get()) {
            try {
                Thread.sleep(50);
            }
            catch (Exception e) {

            }
        }
        return result.get();
    }

    public static ClassType chooseInstance(JFrame owner, JPanel fieldsPanel, boolean addListener) {
        selectedType = null;

        List<ClassType> availableClasses = SyntaxTree.getClasses().stream()
                .sorted(Comparator.comparing(ClassType::getName))
                .toList();

        // Use a modal dialog to block until selection is made
        JDialog instanceDialog = new JDialog(owner, "Choose Instance", true);
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
        JTextField nameField = new JTextField(15);
        if (addListener) {
            JLabel nameLabel = new JLabel("Name:");
            instanceDialog.add(nameLabel, gbc);
            gbc.gridx = 1;
            nameField.setPreferredSize(new Dimension(150, nameField.getPreferredSize().height));
            instanceDialog.add(nameField, gbc);
        }

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

        JScrollPane instanceScrollPane = new JScrollPane(instanceListPanel);
        instanceDialog.add(instanceScrollPane, gbc);

        // Method to refresh the list based on the search term
        Runnable refreshInstanceList = () -> {
            String searchText = searchField.getText().toLowerCase();
            instanceListPanel.removeAll(); // Clear current buttons

            // Re-add filtered buttons
            for (ClassType availableClass : availableClasses) {
                if (availableClass.getName().toLowerCase().contains(searchText)) {
                    JButton classButton = new JButton(availableClass.getName());
                    if (addListener) {
                        classButton.addActionListener(addListener(nameField, availableClass, arrayField, instanceDialog, fieldsPanel));
                    } else {
                        classButton.addActionListener(terminator(instanceDialog, availableClass));
                    }
                    instanceListPanel.add(classButton);
                }
            }

            instanceListPanel.revalidate(); // Revalidate to refresh the UI
            instanceListPanel.repaint();
        };

        // Add DocumentListener to searchField to trigger filtering
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshInstanceList.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshInstanceList.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshInstanceList.run();
            }
        });

        // Initial population of instance list
        refreshInstanceList.run();

        instanceDialog.pack();
        instanceDialog.setLocationRelativeTo(null);  // Center the dialog
        instanceDialog.setVisible(true);  // This will block until the dialog is closed

        return selectedType;  // Return the selected class after the dialog is closed
    }

    public static void rectEditorEntryPanel(String name, String type, boolean primitive, String value, JPanel fieldsPanel) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setSize(15, 300);
        fieldPanel.setLocation(0, 20 * classEntryCounterForRectMenu);
        classEntryCounterForRectMenu++;
        JTextField fieldNameFieldDisplay = new JTextField(name, 15);
        fieldNameFieldDisplay.setEnabled(false);
        fieldNameFieldDisplay.setDisabledTextColor(Color.BLACK);
        JTextField fieldTypeField = new JTextField(type, 40);
        fieldTypeField.setEnabled(false);
        fieldTypeField.setDisabledTextColor(Color.BLACK);
        JCheckBox isPrimitive = new JCheckBox("Primitive", primitive);
        isPrimitive.setEnabled(false);
        JTextField fieldValueFieldDisplay = new JTextField(value, 15);
        fieldValueFieldDisplay.setEnabled(false);
        fieldValueFieldDisplay.setDisabledTextColor(Color.BLACK);
        JButton removeFieldButton = new JButton("-");

        fieldPanel.add(new JLabel("Field:"));
        fieldPanel.add(fieldNameFieldDisplay);
        fieldPanel.add(fieldTypeField);
        fieldPanel.add(isPrimitive);
        fieldPanel.add(fieldValueFieldDisplay);
        fieldPanel.add(removeFieldButton);

        removeFieldButton.addActionListener(e1 -> {
            fieldsPanel.remove(fieldPanel);
            fieldsPanel.revalidate();
            fieldsPanel.repaint();
            classEntryCounterForRectMenu--;
        });
        fieldsPanel.add(fieldPanel);
    }


    public static ActionListener addListener(JTextField nameField, ClassType c, JTextField arrayCnt, JDialog instanceFrame, JPanel fieldsPanel) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.getText().isEmpty()) {
                    selectedType = c;

                    rectEditorEntryPanel(nameField.getText(), repeatString("array ", Integer.parseInt(arrayCnt.getText())) + c.name, false, "", fieldsPanel);
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
        titleBar.setBorder(BorderFactory.createEmptyBorder());
        return titleBar;
    }



    public static void setResizer() {
        if (os == OS.WINDOWS) {
            ComponentResizer cr = new ComponentResizer();
            cr.setMinimumSize(new Dimension(300, 300));
            cr.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            cr.registerComponent(mainFrame);
            cr.setSnapSize(new Dimension(10, 10));
        }
    }

    public static void getPasswordFromUser() {
        // Create a text field for password input
        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar('*');


        // Create the dialog with a custom panel
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Enter sudo password:"));
        panel.add(passwordField);

        // Create and configure the dialog
        JDialog dialog = new JDialog((Frame) null, "Password Input", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            char[] passwordArray = passwordField.getPassword();
            Executor.ramTempVersionOfSudo = new String(passwordArray);
            Executor.pwdSet = true;
            dialog.dispose();
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            Executor.pwdSet = true;
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(null);

        // Show the dialog and request focus for the password field
        SwingUtilities.invokeLater(() -> {
            dialog.setVisible(true);
            passwordField.requestFocusInWindow();
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyTyped(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    char[] passwordArray = passwordField.getPassword();
                    Executor.ramTempVersionOfSudo = new String(passwordArray);
                    Executor.pwdSet = true;
                    dialog.dispose();
                }
            }
        });

        // Add a window listener to handle the closing action (the "X" button)
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ensure pwdSet is true if the window is closed by the "X" button
                Executor.pwdSet = true;
                dialog.dispose();
            }
        });

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


    public static void createSelectClassTypeWindow(List<ClassType> objects) {
        // Create a JFrame to hold the window
        JFrame frame = new JFrame("Select an Object");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 400);

        // Create a JPanel to hold the buttons
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create a JScrollPane to allow scrolling
        JScrollPane scrollPane = new JScrollPane(panel);

        // A holder for the selected object
        selectedObject = null;
        obectSelected = false;

        // Add a button for each object in the ArrayList
        for (ClassType obj : objects.stream().sorted().toList()) {
            JButton button = new JButton(obj.name);
            panel.add(button);  // Add the button to the panel, not the scrollPane

            // Add an ActionListener to return the selected object
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedObject = obj;
                    obectSelected = true;
                    frame.dispose(); // Close the window
                }
            });
        }

        // Add the scrollPane to the frame
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }


    public static Object selectClassType(List<ClassType> objects) {
        createSelectClassTypeWindow(objects);

        // Block until the frame is closed
        while (!obectSelected) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Return the selected object
        return selectedObject;
    }

    public static void openFolderWindow(String folderPath) {
        // Create a JFrame (the main window)
        JFrame frame = new JFrame("Version Selector");
        frame.setSize(400, 400);

        // Create a JScrollPane with a JPanel inside it
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);

        // Get the list of folders in the specified directory
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles(File::isDirectory);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                // Create a button for each folder
                JButton button = new JButton(file.getName());
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Print the name of the folder and close the window
                        InputHandler.setEvoAlVersion(file.getName());
                        frame.dispose();
                    }
                });
                panel.add(button);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "The folder path is invalid or there are no subfolders.");
        }

        // Add the scroll pane to the frame and make it visible
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    public static void openPresetWindow() {
        // Create a JFrame (the main window)
        JFrame frame = new JFrame("Preset Selector");
        frame.setSize(400, 400);

        // Create a JScrollPane with a JPanel inside it
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);

        for (var preset : Preset.presets) {
            // Create a button for each folder
            JButton button = new JButton(preset.getDisplayName());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Print the name of the folder and close the window
                    Main.preset = preset;
                    InputHandler.actionHandler.changesMade();
                    frame.dispose();
                    ErrorPane.checkForErrors();
                    mainFrame.revalidate();
                    mainFrame.repaint();
                }
            });
            panel.add(button);
        }

        // Add the scroll pane to the frame and make it visible
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    public static void centerFrame(JFrame frame) {
        frame.setLocationRelativeTo(null);
    }

    public static void fromPreset() {
        // Create a JFrame (the main window)
        JFrame frame = new JFrame("Preset Selector");
        frame.setSize(400, 400);
        centerFrame(frame);


        // Create a JScrollPane with a JPanel inside it
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);

        for (var preset : Preset.presets) {
            // Create a button for each folder
            JButton button = new JButton(preset.getDisplayName());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Print the name of the folder and close the window
                    var cancle = showUnsaveDialog();
                    if (!cancle) {
                        if (FileManager.newFile()) {
                            Main.preset = preset;
                            for (var r : preset.requiredRectNames) {
                                mainPanel.leftPanel.addRect(RectFactory.getRectFromClassType(SyntaxTree.get(r)));
                            }
                            FileManager.save();
                            frame.dispose();
                            ErrorPane.checkForErrors();
                            mainFrame.revalidate();
                            mainFrame.repaint();
                        }
                    }
                }
            });
            panel.add(button);
        }

        // Add the scroll pane to the frame and make it visible
        frame.add(scrollPane);
        frame.setVisible(true);
    }


    public static void newGeneratorFile() {
        String cur = cacheManager.getFirstElement(String.class, "filesOpened");
        if (cur != null) {
            File file = new File(cur);
            file = new File(file.getParentFile().getAbsolutePath() + "/generator." + Main.saveFormat);
            // Check if a file with the given name already exists
            if (file.exists()) {
                JOptionPane.showMessageDialog(null, "A file with that name already exists. Please choose a different name.", "Error", JOptionPane.ERROR_MESSAGE);

            }
            else {
                FileManager.emptySave();
                writeJSONToFile(createSave(), file.getAbsolutePath());
                Main.cacheManager.addToBuffer("filesOpened", file.getAbsolutePath());
                Main.preset = Preset.getPreset("generator");
                for (var r : preset.requiredRectNames) {
                    mainPanel.leftPanel.addRect(RectFactory.getRectFromClassType(SyntaxTree.get(r)));
                }
                FileManager.save();
                ErrorPane.checkForErrors();
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        }


    }




}
