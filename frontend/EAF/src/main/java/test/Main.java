package test;

import compiler.SyntaxTree;
import intro.DoubleHelixAnimation;
import test.rects.*;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Main extends JPanel {

    public static Color bgColor = new Color(49, 51, 53);

    public static Color dividerColor = new Color(35, 35, 35);

    public static Color scrollBarBg = new Color(43, 43, 43);

    public static Color searchBarError = new Color(200, 100, 100);

    public static Color scrollBarButton = new Color(150, 150, 150);

    public static Color scrollBarTopAndBottom = new Color(60, 60, 60);

    public static Color searchBarText = new Color(255, 255, 255);

    public static Color searchBar = new Color(100, 100, 100);
    public static Color searchBarBorder = new Color(85, 85, 85);

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();

    public static int arrayDefaultCount = 1;

    public JSplitPane mainSplitPane = null;

    public static Main mainPanel = null;

    public static DataFieldListPane dataPanel = null;

    public static JFrame mainFrame = null;

    // Declare the text field and new panel
    JTextField rightPanelTextField;
    public JTextField leftPanelTextField;
    private JPanel rightContainerPanel;
    private FolderPanel folderPanel;

    public static String saveFormat = "eaf";

    public static String savesPath = "/saves";

    public static CacheManager cacheManager = new CacheManager();

    static void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = scrollBarButton;
                this.trackColor = scrollBarBg;
                this.thumbDarkShadowColor = scrollBarBg;
                this.thumbHighlightColor = scrollBarButton;
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
                button.setBackground(scrollBarTopAndBottom);
                button.setForeground(scrollBarButton);
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
                this.thumbColor = scrollBarButton;
                this.trackColor = scrollBarBg;
                this.thumbDarkShadowColor = scrollBarBg;
                this.thumbHighlightColor = scrollBarButton;
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
                button.setBackground(scrollBarTopAndBottom);
                button.setForeground(scrollBarButton);
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
            g.setColor(dividerColor);
            // Fill the divider with the chosen color
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public Main(int numRects) {
        // Create the "Filter:" label and the content label
        JLabel filterLabel = new JLabel("Filter:");
        InputHandler.contentLabel = new JLabel("");

        this.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        mainPanel = this;
        // Initialize the text field and new panel
        rightPanelTextField = new JTextField();
        rightPanelTextField.setBackground(searchBar);
        rightPanelTextField.setForeground(searchBarText);
        rightPanelTextField.setCaretColor(searchBarText);
        rightPanelTextField.setSelectionColor(searchBarText);
        rightPanelTextField.setSelectedTextColor(searchBar);
        Border border = BorderFactory.createEmptyBorder();
        rightPanelTextField.setBorder(border);

        leftPanelTextField = new JTextField();
        leftPanelTextField.setBackground(searchBar);
        leftPanelTextField.setForeground(searchBarText);
        leftPanelTextField.setCaretColor(searchBarText);
        leftPanelTextField.setSelectionColor(searchBarText);
        leftPanelTextField.setSelectedTextColor(searchBar);
        leftPanelTextField.setBorder(border);

        dataPanel = new DataFieldListPane();
        dataPanel.setBackground(bgColor);
        dataPanel.setBackground(bgColor);
        customizeScrollBar(dataPanel);


        folderPanel = new FolderPanel(SyntaxTree.getNonAbstractClasses());
        customizeScrollBar(rightPanel);
        customizeScrollBar(leftPanel);

        rightPanelTextField.addKeyListener(new KeyListener() {
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

        rightPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InputHandler.filterChanged();
            }
        });

        rightPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
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



        // Create a panel to hold the text field and the right panel
        rightContainerPanel = new JPanel();
        rightContainerPanel.setLayout(new BorderLayout());



        // Set background and foreground colors for the labels
        filterLabel.setOpaque(true);
        filterLabel.setBackground(searchBar);
        filterLabel.setForeground(searchBarText);

        InputHandler.contentLabel.setOpaque(true);
        InputHandler.contentLabel.setBackground(searchBar);
        InputHandler.contentLabel.setForeground(searchBarText);

        // Use GridBagLayout to arrange the components
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(searchBar); // Set background for the panel
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
        panel.add(rightPanelTextField, gbc);

        // Place the content label on the right
        gbc.gridx = 2; // Column 2
        gbc.fill = GridBagConstraints.NONE; // Do not stretch the label
        gbc.weightx = 0.0; // Do not allow the label to grow
        panel.add(InputHandler.contentLabel, gbc);

        panel.setBackground(searchBar);

        // Add the panel to the top of the container
        JPanel rightContainerPanel = new JPanel(new BorderLayout());
        rightContainerPanel.setBackground(searchBar);
        panel.setBorder(BorderFactory.createLineBorder(searchBarBorder, 1));
        rightContainerPanel.add(panel, BorderLayout.NORTH);








        leftPanelTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                InputHandler.searchChanged();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    InputHandler.current = 0;
                    var res = InputHandler.getStringMarkers();
                    int value = leftPanel.getVerticalScrollBar().getValue();
                    int maximum = leftPanel.getVerticalScrollBar().getMaximum();
                    int visibleAmount = leftPanel.getVerticalScrollBar().getVisibleAmount();

                    // Determine if the scrollbar is scrolled fully down
                    boolean isScrolledFullyDown = (value + visibleAmount >= maximum);

                    if (!res.isEmpty() && isScrolledFullyDown) {
                        leftPanel.getVerticalScrollBar().setValue(res.get(0));
                        InputHandler.current = 1;
                    }
                    else {
                        int i = leftPanel.getVerticalScrollBar().getValue();
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
                        leftPanel.getVerticalScrollBar().setValue(i);
                    }

                }
                InputHandler.searchChanged();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                InputHandler.searchChanged();
            }
        });

        leftPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InputHandler.searchChanged();
            }
        });

        leftPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                var res = InputHandler.getStringMarkers();
                if (!res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
                    leftPanel.getVerticalScrollBar().setValue(res.get(0));
                    InputHandler.current = 1;
                }
                if (res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
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





        InputHandler.contentLabel2 = new JLabel("");
        InputHandler.contentLabel2.setOpaque(true);
        InputHandler.contentLabel2.setBackground(searchBar);
        InputHandler.contentLabel2.setForeground(searchBarText);

        var filterLabel2 = new JLabel("Search");
        filterLabel2.setOpaque(true);
        filterLabel2.setBackground(searchBar);
        filterLabel2.setForeground(searchBarText);

        // Use GridBagLayout to arrange the components
        JPanel panel2 = new JPanel(new GridBagLayout());
        panel2.setBackground(searchBar); // Set background for the panel
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
        panel2.add(leftPanelTextField, gbc2);

        // Place the content label on the right
        gbc2.gridx = 2; // Column 2
        gbc2.fill = GridBagConstraints.NONE; // Do not stretch the label
        gbc2.weightx = 0.0; // Do not allow the label to grow
        panel2.add(InputHandler.contentLabel2, gbc2);

        panel2.setBackground(searchBar);

        // Add the panel to the top of the container
        JPanel leftContainerPanel = new JPanel(new BorderLayout());
        leftContainerPanel.setBackground(searchBar);
        panel2.setBorder(BorderFactory.createLineBorder(searchBarBorder, 1));
        leftContainerPanel.add(panel2, BorderLayout.NORTH);

















        leftContainerPanel.add(leftPanel, BorderLayout.CENTER);
        leftContainerPanel.setBorder(BorderFactory.createEmptyBorder());




        rightPanel.setBackground(searchBar);
        rightContainerPanel.add(rightPanel, BorderLayout.CENTER);
        rightContainerPanel.setBorder(BorderFactory.createEmptyBorder());

        // Create a vertical split pane to hold the new panel and the right container panel
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, folderPanel, rightContainerPanel);
        rightSplitPane.setDividerLocation(100); // Initial divider location
        rightSplitPane.setResizeWeight(0.1); // Adjust resize weight as needed
        rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
        rightSplitPane.setUI(new CustomSplitPaneUI());
        rightSplitPane.setBackground(bgColor);





        // Create a vertical split pane for the left side
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataPanel, leftContainerPanel);
        leftSplitPane.setDividerLocation(100); // Initial divider location
        leftSplitPane.setResizeWeight(0.1); // Adjust resize weight as needed
        leftSplitPane.setBorder(BorderFactory.createEmptyBorder());
        leftSplitPane.setUI(new CustomSplitPaneUI());
        leftSplitPane.setBackground(bgColor);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, rightSplitPane);
        mainSplitPane.setDividerLocation(400); // Initial divider location
        mainSplitPane.setResizeWeight(0.5); // Evenly split the panels
        mainSplitPane.setBackground(dividerColor);
        mainSplitPane.setForeground(dividerColor);
        mainSplitPane.setUI(new CustomSplitPaneUI());
        Border border2 = BorderFactory.createLineBorder(dividerColor, 1);
        mainSplitPane.setBorder(border2);




        add(mainSplitPane, BorderLayout.CENTER);


        MouseAdapter focus = new MouseAdapter() {
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

        MouseAdapter mouseAdapter = new MouseAdapter() {
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

        MouseAdapter mouseAdapter2 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());

                // Convert the point from the source component's coordinate system to the left panel's coordinate system
                Point panelRelativePos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), mainFrame);
                var prevSelected = InputHandler.selected;
                InputHandler.unselect();
                Rect matchingRect = leftPanel.getRect(leftPanelPos);
                if (matchingRect instanceof RectWithRects) {
                    matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                    if (matchingRect != null && leftPanel.hasFocus()) {
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e);
                        if (InputHandler.isControlPressed && matchingRect instanceof ClassRect && prevSelected != matchingRect) {
                            InputHandler.setSelected((ClassRect)matchingRect);
                        }
                    }
                }
                if (matchingRect == null) {
                    leftPanel.requestFocusInWindow();
                }
                repaint();

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

        dataPanel.getViewport().getView().addMouseListener(focus);
        dataPanel.getViewport().getView().addMouseMotionListener(focus);

        folderPanel.addMouseListener(focus);
        folderPanel.addMouseMotionListener(focus);

        folderPanel.folderDisplayPanel.addMouseListener(focus);
        folderPanel.folderDisplayPanel.addMouseMotionListener(focus);


        rightPanel.getViewport().getView().addMouseListener(mouseAdapter);
        rightPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
        leftPanel.getViewport().getView().addMouseListener(mouseAdapter2);
        leftPanel.getViewport().getView().addMouseMotionListener(mouseAdapter2);
        InputHandler.addKeyListener(leftPanel);
        InputHandler.addKeyListener(rightPanel);
        InputHandler.addKeyListener(dataPanel);
        InputHandler.addKeyListener(folderPanel.scrollPane);

        FileManager.loadRecent();
    }


    private static void createAndShowGUI(int numRects) {
        // Create the main frame
        mainFrame = new JFrame("EAF");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the File menu
        JMenu testMenu = new JMenu("Test");

        // Create menu items
        JMenuItem newItem = new JMenuItem("Script");

        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.writeToFile(dataPanel.toString(),"config.ddl");
                FileManager.writeToFile(Main.mainPanel.leftPanel.toString(),"config.ol");
            }
        });




        JMenuItem openItem = new JMenuItem("Open");



        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileManager.loadSave(FileManager.readJSONFileToJSON(savesPath + "/test" + "." + saveFormat));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JMenuItem exitItem = new JMenuItem("Save");

        // Add action listeners for menu items
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileManager.writeJSONToFile(FileManager.createSave(), savesPath + "/test" + "." + saveFormat);
            }
        });

        // Add menu items to File menu
        testMenu.add(newItem);
        testMenu.add(openItem);
        testMenu.addSeparator();
        testMenu.add(exitItem);

        // Add File menu to menu bar
        menuBar.add(testMenu);


        // Create the File menu
        JMenu fileMenu = new JMenu("File");

        // Create menu items
        JMenuItem openFileDotDotDot = new JMenuItem("open ...");

        openFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    var file = FileManager.chooseJavaFile(savesPath, saveFormat);
                    if (file != null) {
                        FileManager.loadSave(FileManager.readJSONFileToJSON(file));
                        cacheManager.addToBuffer("filesOpened", file.getPath());
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        fileMenu.add(openFileDotDotDot);


        JMenuItem saveFileDotDotDot = new JMenuItem("save as ...");

        saveFileDotDotDot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var file = FileManager.saveJavaFile(savesPath, saveFormat, "save");
                if (file != null) {
                    FileManager.writeJSONToFile(FileManager.createSave(), file.getPath());
                    System.out.println("File " + file.getName() + " saved!");
                }
            }
        });

        fileMenu.add(saveFileDotDotDot);

        menuBar.add(fileMenu);



        // Set the menu bar to the frame
        mainFrame.setJMenuBar(menuBar);

        // Create and add content to the frame
        new Main(numRects);
        mainFrame.add(mainPanel);

        // Set frame size and location
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }


    public static void main(String[] args) throws Exception {
        int numRects = 20;  // Change this number to create more or fewer rectangles
        try {
            var s = DoubleHelixAnimation.create();
            s.objective = "Constructing Syntax-Tree";
            SyntaxTree.start();
            s.stop();
            while (s.isUnfinished()) {
                Thread.sleep(100);
            }
        }
        catch (Exception e) {
            throw new Exception(e);
        }

        SwingUtilities.invokeLater(() -> createAndShowGUI(numRects));
    }


}