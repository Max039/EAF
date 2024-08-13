package test;

import action.ActionHandler;
import action.AddedRectAction;
import action.DeletedRectAction;
import action.MovedRectAction;
import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import compiler.SyntaxTree;
import intro.DoubleHelixAnimation;
import org.json.JSONArray;
import org.json.JSONObject;
import test.rects.*;
import test.rects.multi.ArrayRect;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DragDropRectanglesWithSplitPane extends JPanel {

    public static Color bgColor = new Color(49, 51, 53);

    public static Color dividerColor = new Color(35, 35, 35);

    public static Color scrollBarBg = new Color(43, 43, 43);

    public static Color searchBarError = new Color(200, 100, 100);

    public static Color scrollBarButton = new Color(150, 150, 150);

    public static Color scrollBarTopAndBottom = new Color(60, 60, 60);

    public static Color searchBarText = new Color(255, 255, 255);

    public static Color searchBar = new Color(100, 100, 100);
    public static Color searchBarBorder = new Color(85, 85, 85);

    public static RectWithRects draggingSource = null;

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();
    public Rect draggedRect = null;
    private Point dragOffset = null;

    public JSplitPane mainSplitPane = null;

    public static DragDropRectanglesWithSplitPane subFrame = null;

    public static DataFieldListPane dataPanel = null;

    public static JFrame mainFrame = null;

    public static boolean showButtons = false;

    // Declare the text field and new panel
    private JTextField rightPanelTextField;
    public JTextField leftPanelTextField;
    private JPanel rightContainerPanel;
    private FolderPanel folderPanel;

    public HashMap<Rect, String> erroRects = new HashMap<>();

    public JLabel contentLabel;
    public JLabel contentLabel2;

    public HashMap<String, ArrayList<Integer>> stringMarker = new HashMap<>();

    int current = 0;

    public static ActionHandler actionHandler = new ActionHandler();

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
    }


    public static <T extends Rect> T getRectFromClassType(ClassType type) {

        int length = type.fields.size();
        var names = new String[length];
        var types = new FieldType[length];
        var rects = new Rect[length];
        int i = 0;
        for (var field : type.fields.entrySet()) {
            names[i] = field.getKey();
            types[i] = field.getValue().getFirst();
            rects[i] = getRectFromFieldType(field.getValue().getFirst(), field.getValue().getSecond());
           i++;
        }
        return (T) new ClassRect(RectPanel.instanceWidth, RectPanel.instanceHeight, RectPanel.instanceColor, type, names, rects, types);
    }

    public static <T extends Rect> T getRectFromFieldType(FieldType type, FieldValue value) {

        if (value != null) {
            if (type.arrayCount > 0) {
                int length = value.values.size();
                var clazz = new ClassType(type.typeName, null, "Array");
                var names = new String[length];
                var types = new FieldType[length];
                var rects = new Rect[length];

                int i = 0;
                for (var item : value.values) {
                    names[i] = "";
                    types[i] = item.type;
                    rects[i] = getRectFromFieldType(item.type, item);
                    i++;
                }

                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;
                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz, ctype, names, rects, types, type.primitive);

            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    return (T) new TextFieldRect(value.value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, false);
                }
                else {
                    return getRectFromClassType(value.instance);
                }
            }
        }
        else {
            if (type.arrayCount > 0) {
                var clazz = new ClassType(type.typeName, null, "Array");
                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;
                boolean fill = type.primitive;
                if (!fill) {
                    var check = SyntaxTree.classRegister.get(type.typeName).findSingleNonAbstractClass();
                    if (check != null) {
                        System.out.println("Info: Only 1 non abstract type available for " + type.typeName + " converting array to " + check.name);
                        fill = true;
                        ctype = new FieldType(check.name, false, ctype.arrayCount);
                    }
                }

                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz,  ctype, 3, fill);
            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    String content;
                    if (type.typeName.toLowerCase().contains("string") ) {
                        content = "Enter string here!";
                    }
                    else if (type.typeName.toLowerCase().contains("int") ||  type.typeName.toLowerCase().contains("real"))  {
                        content = "0";
                    } else if (type.typeName.toLowerCase().contains("bool")) {
                        var r = new ArrayList<Object>();
                        r.add(true);
                        r.add(false);
                        return (T) new OptionsFieldRect(r, "true", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                    }
                    else if (type.typeName.toLowerCase().contains("data")) {
                        return (T) new OptionsFieldRect(dataPanel.getDataFieldList(), "", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                    }
                    else if (type.typeName.toLowerCase().contains("literal")) {
                        content  = "Enter literal here!";
                    }
                    else {
                        content  = "Unkown primitive";
                        System.out.println("Unkown primitve: " + type.typeName);
                    }
                    return (T) new TextFieldRect(content, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                }
                else {
                    var check = SyntaxTree.classRegister.get(type.typeName).findSingleNonAbstractClass();
                    if (check != null) {
                        System.out.println("Info: Only 1 non abstract type available for " + type.typeName + " creating and setting instance of " + check.name + " for field.");
                        return (T) getRectFromClassType(check);
                    }
                    else {
                        return null;
                    }
                }
            }
        }
    }

    public void setPosOfDraggingRect(MouseEvent e) {
        Point rightPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
        Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
        int newX = rightPanelPos.x - dragOffset.x;
        int newY = rightPanelPos.y - dragOffset.y;
        leftPanel.draggingRect.setPosition(newX + leftPanel.getWidth() + mainSplitPane.getDividerSize(), leftPanelPos.y  - dragOffset.y);
        rightPanel.draggingRect.setPosition(newX, newY);
    }

    public void setDraggingRect(Rect rect, MouseEvent e, Point offset, RectWithRects source) {
        draggingSource = source;
        draggedRect = rect;
        dragOffset = new Point(offset.x, offset.y);
        rightPanel.setDraggingRect(draggedRect.clone());
        leftPanel.setDraggingRect(draggedRect);
        setPosOfDraggingRect(e);
    }

    public void filterChanged() {
        rightPanel.filter = rightPanelTextField.getText();
        rightPanel.getVerticalScrollBar().setValue(0);
        contentLabel.setText(Long.toString(rightPanel.getMatchingRects()));
        contentLabel.revalidate();
        contentLabel.repaint();
        revalidate();
        repaint();
    }

    public ArrayList<Integer> getStringMarkers() {
        return new ArrayList<>(stringMarker.entrySet().stream().filter(t -> t.getKey().toLowerCase().contains(leftPanelTextField.getText().toLowerCase())).map(Map.Entry::getValue).flatMap(Collection::stream).sorted().toList());
    }

    public void searchChanged() {
        var res = getStringMarkers();
        if (res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
            leftPanelTextField.setForeground(searchBarError);
        }
        else {
            leftPanelTextField.setForeground(searchBarText);
        }
        if (leftPanelTextField.getText().isEmpty()) {
            contentLabel2.setText("");
        }
        else {
            contentLabel2.setText(current + "/" + res.size());
        }

        revalidate();
        repaint();
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

    public DragDropRectanglesWithSplitPane(int numRects) {
        // Create the "Filter:" label and the content label
        JLabel filterLabel = new JLabel("Filter:");
        contentLabel = new JLabel("");

        this.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        subFrame = this;
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
                filterChanged();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                filterChanged();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                filterChanged();
            }
        });

        rightPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterChanged();
            }
        });

        rightPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterChanged();
            }
        });



        // Create a panel to hold the text field and the right panel
        rightContainerPanel = new JPanel();
        rightContainerPanel.setLayout(new BorderLayout());



        // Set background and foreground colors for the labels
        filterLabel.setOpaque(true);
        filterLabel.setBackground(searchBar);
        filterLabel.setForeground(searchBarText);

        contentLabel.setOpaque(true);
        contentLabel.setBackground(searchBar);
        contentLabel.setForeground(searchBarText);

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
        panel.add(contentLabel, gbc);

        panel.setBackground(searchBar);

        // Add the panel to the top of the container
        JPanel rightContainerPanel = new JPanel(new BorderLayout());
        rightContainerPanel.setBackground(searchBar);
        panel.setBorder(BorderFactory.createLineBorder(searchBarBorder, 1));
        rightContainerPanel.add(panel, BorderLayout.NORTH);








        leftPanelTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                searchChanged();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    current = 0;
                    var res = getStringMarkers();
                    int value = leftPanel.getVerticalScrollBar().getValue();
                    int maximum = leftPanel.getVerticalScrollBar().getMaximum();
                    int visibleAmount = leftPanel.getVerticalScrollBar().getVisibleAmount();

                    // Determine if the scrollbar is scrolled fully down
                    boolean isScrolledFullyDown = (value + visibleAmount >= maximum);

                    if (!res.isEmpty() && isScrolledFullyDown) {
                        leftPanel.getVerticalScrollBar().setValue(res.get(0));
                        current = 1;
                    }
                    else {
                        int i = leftPanel.getVerticalScrollBar().getValue();
                        boolean found = false;

                        for (var y : res) {
                            current++;
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
                searchChanged();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                searchChanged();
            }
        });

        leftPanelTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchChanged();
            }
        });

        leftPanelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                var res = getStringMarkers();
                if (!res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
                    leftPanel.getVerticalScrollBar().setValue(res.get(0));
                    current = 1;
                }
                if (res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
                    current = 0;
                }
                searchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchChanged();
            }
        });





        contentLabel2 = new JLabel("");
        contentLabel2.setOpaque(true);
        contentLabel2.setBackground(searchBar);
        contentLabel2.setForeground(searchBarText);

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
        panel2.add(contentLabel2, gbc2);

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
                DragDropRectanglesWithSplitPane.this.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                DragDropRectanglesWithSplitPane.this.mouseReleased(e);
            }


        };

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                rightPanel.requestFocusInWindow();

                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());

                Rect rect = rightPanel.getRect(point);

                if (rect != null && rect.contains(point)) {
                    setDraggingRect(rect.clone(), e, new Point(point.x - rect.getX(), point.y - rect.getY()), null);
                }

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                DragDropRectanglesWithSplitPane.this.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                DragDropRectanglesWithSplitPane.this.mouseReleased(e);
            }


        };

        MouseAdapter mouseAdapter2 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());

                // Convert the point from the source component's coordinate system to the left panel's coordinate system
                Point panelRelativePos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), mainFrame);


                Rect matchingRect = leftPanel.getRect(leftPanelPos);
                if (matchingRect instanceof RectWithRects) {
                    matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                    if (matchingRect != null && leftPanel.hasFocus()) {
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e);

                    }
                }
                if (matchingRect == null) {
                    leftPanel.requestFocusInWindow();
                }
                repaint();

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                DragDropRectanglesWithSplitPane.this.mouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                DragDropRectanglesWithSplitPane.this.mouseReleased(e);
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
        addKeyListener(leftPanel);
        addKeyListener(rightPanel);
        addKeyListener(dataPanel);
        addKeyListener(folderPanel.scrollPane);

    }

    public static void writeToFile(String content, String filePath) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
            writer.write(content);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("An error occurred while closing the writer.");
                e.printStackTrace();
            }
        }
    }


    private static void createAndShowGUI(int numRects) {
        // Create the main frame
        mainFrame = new JFrame("EAF");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the File menu
        JMenu fileMenu = new JMenu("File");

        // Create menu items
        JMenuItem newItem = new JMenuItem("Script");

        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeToFile(dataPanel.toString(),"config.ddl");
                writeToFile(DragDropRectanglesWithSplitPane.subFrame.leftPanel.toString(),"config.ol");
            }
        });




        JMenuItem openItem = new JMenuItem("Open");



        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JSONObject o = readJSONFileToJSON("test.json");
                    JSONArray a2 = o.getJSONArray("data");
                    dataPanel.fromJson(a2);
                    JSONArray a = o.getJSONArray("rects");
                    subFrame.leftPanel.fromJson(a);
                    actionHandler.reset();
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
                JSONObject o = new JSONObject();
                o.put("rects", subFrame.leftPanel.toJson());
                o.put("data", dataPanel.toJson());
                writeJSONToFile(o, "test.json");
            }
        });

        // Add menu items to File menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Add File menu to menu bar
        menuBar.add(fileMenu);

        // Set the menu bar to the frame
        mainFrame.setJMenuBar(menuBar);

        // Create and add content to the frame
        new DragDropRectanglesWithSplitPane(numRects);
        mainFrame.add(subFrame);

        // Set frame size and location
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }


    public <T extends JScrollPane> void addKeyListener(T j) {
        j.addKeyListener(new KeyAdapter() {
            private boolean isControlPressed = false;
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isControlPressed = true;
                }
                else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    showButtons = true;
                    revalidate();
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Z && isControlPressed) {
                    actionHandler.ctrlZ();
                    revalidate();
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Y && isControlPressed) {
                    actionHandler.ctrlY();
                    revalidate();
                    repaint();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isControlPressed = false;
                }
                else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    showButtons = false;
                    revalidate();
                    repaint();
                }
            }
        });

        j.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                showButtons = false;
                revalidate();
                repaint();
            }
        });
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

    public void mouseDragged(MouseEvent e) {
        if (draggedRect != null) {
            leftPanel.drawDragging = true;
            rightPanel.drawDragging = true;

            Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
            setPosOfDraggingRect(e);

            Rect matchingRect = leftPanel.getRect(leftPanelPos);
            if (matchingRect instanceof RectWithRects) {
                matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                if (matchingRect != null) {
                    var res = matchingRect.onHover(leftPanelPos);


                    if (!res.getFirst()) {
                        leftPanel.drawDragging = false;
                        rightPanel.drawDragging = false;
                    }
                    if (res.getSecond()) {
                        leftPanel.draggingRect.setTransparent();
                        rightPanel.draggingRect.setTransparent();
                    }
                    else {
                        leftPanel.draggingRect.setOpace();
                        rightPanel.draggingRect.setOpace();
                    }
                }
            }
            repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (draggedRect != null) {
            Point releasePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
            Point pointFromLeft = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel);
            if (leftPanel.contains(pointFromLeft)) {
                Rect matchingRect = leftPanel.getRect(releasePoint);
                if (matchingRect instanceof RectWithRects) {
                    int p = draggedRect.parentIndex;
                    var res = ((RectWithRects)matchingRect).setIndex(releasePoint, draggedRect);
                    if (res.getFirst()) {
                        if (draggingSource != null) {
                            actionHandler.action(new MovedRectAction(draggingSource, (RectWithRects)res.getSecond().getFirst(), draggedRect, p, res.getSecond().getSecond()));
                        }
                        else {
                            actionHandler.action(new AddedRectAction((RectWithRects)res.getSecond().getFirst(), draggedRect, res.getSecond().getSecond()));
                        }
                        draggedRect.addTo(leftPanel.drawingPanel);
                    }
                    else {
                        actionHandler.action(new DeletedRectAction(draggingSource, draggedRect, p));
                    }

                }
                else if (matchingRect == null) {
                    if (draggingSource != null) {
                        actionHandler.action(new MovedRectAction(draggingSource, null, draggedRect, draggedRect.parentIndex, leftPanel.getRects().size()));
                    }
                    else {
                        actionHandler.action(new AddedRectAction(null, draggedRect, leftPanel.getRects().size()));
                    }
                    leftPanel.addRect(leftPanel.draggingRect);
                }
                else {
                    actionHandler.action(new DeletedRectAction(draggingSource, draggedRect, draggedRect.parentIndex));
                }
                leftPanel.requestFocusInWindow();
            }

            draggingSource = null;
            draggedRect = null;
            dragOffset = null;
            leftPanel.clearDraggingRect();
            rightPanel.clearDraggingRect();
            leftPanel.mouseReleased();
            rightPanel.mouseReleased();
            revalidate();
            repaint();
        }
    }


    public void clearErrors() {
        erroRects = new HashMap<>();
    }


    public void checkForErrors(Rect r) {
        r.setValidity();
        if (r instanceof RectWithRects) {
            for (var t : ((RectWithRects) r).getSubRects()) {
                if (t != null) {
                    checkForErrors(t);
                }
            }
        } else if (r instanceof OptionsFieldRect) {
            ((OptionsFieldRect)r).refreshComboBoxOptions();
        }

    }

    public void checkForErrors() {
        clearErrors();
        System.out.println("Checking for errors");
        for (var r : leftPanel.getRects()) {
            checkForErrors(r);
        }

        for (var e : erroRects.values()) {
            System.out.println("Error in rect: " + e);
        }

    }

    public static void writeJSONToFile(JSONObject jsonArray, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonArray.toString(4)); // The "4" here is for pretty-printing with an indentation of 4 spaces
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJSONFileToJSON(String filePath) throws IOException, org.json.JSONException {
        // Open the file using a BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read the entire content of the file into a String
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Convert the String content to a JSONArray
            return new JSONObject(jsonContent.toString());
        }
    }


}