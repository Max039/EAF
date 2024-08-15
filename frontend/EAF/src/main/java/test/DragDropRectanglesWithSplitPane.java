package test;

import action.*;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
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

    public static boolean isControlPressed = false;

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();
    public Rect draggedRect = null;
    private Point dragOffset = null;

    public static int arrayDefaultCount = 1;

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

    public static ClassRect selected = null;

    public static ClassRect clipBoard = null;

    public HashMap<Rect, String> erroRects = new HashMap<>();

    public JLabel contentLabel;
    public JLabel contentLabel2;

    public HashMap<String, ArrayList<Integer>> stringMarker = new HashMap<>();

    int current = 0;

    public static ActionHandler actionHandler = new ActionHandler();

    public static String saveFormat = "eaf";

    public static String savesPath = "/saves";

    public static CacheManager cacheManager = new CacheManager();

    public void setSelected(ClassRect r) {
        selected = r ;
        selected.select();
    }

    public void unselect() {
        if (selected != null) {
            selected.unselect();
            selected = null;
        }
    }

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
        return (T) new ClassRect(RectPanel.instanceWidth, RectPanel.instanceHeight, RectPanel.instanceColor, type, names, rects, types, false);
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
                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz, ctype, names, rects, types, type.primitive, true);

            }
            else {
                Rect preR;
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    String content;
                    if (type.typeName.toLowerCase().contains("int") ||  type.typeName.toLowerCase().contains("real") || type.typeName.toLowerCase().contains("literal") || type.typeName.toLowerCase().contains("string") )  {
                        content = value.value;
                    } else if (type.typeName.toLowerCase().contains("bool")) {
                        var r = new ArrayList<Object>();
                        r.add(true);
                        r.add(false);
                        return (T)  new OptionsFieldRect(r, value.value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, TextFieldRect.uneditableColor);
                    }
                    else if (type.typeName.toLowerCase().contains("data")) {

                        return (T)  new OptionsFieldRect(dataPanel.getDataFieldList(), value.value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, TextFieldRect.uneditableColor);
                    }
                    else {
                        content  = "Unkown primitive";
                        System.out.println("Unkown primitve: " + type.typeName);
                    }
                    preR = new TextFieldRect(content, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                    ((TextFieldRect) preR).setTextColor(TextFieldRect.uneditableColor);
                    return (T)  preR;
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

                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz,  ctype, arrayDefaultCount, fill);
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
                        return (T) new OptionsFieldRect(r, "true", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, OptionsFieldRect.defaultTextColor);
                    }
                    else if (type.typeName.toLowerCase().contains("data")) {
                        return (T) new OptionsFieldRect(dataPanel.getDataFieldList(), "", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, OptionsFieldRect.defaultTextColor);
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
                        var newR = (ClassRect) getRectFromClassType(check);
                        newR.setLocked(true);
                        return (T) newR ;
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
                var prevSelected = selected;
                unselect();
                Rect matchingRect = leftPanel.getRect(leftPanelPos);
                if (matchingRect instanceof RectWithRects) {
                    matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                    if (matchingRect != null && leftPanel.hasFocus()) {
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e);
                        if (isControlPressed && matchingRect instanceof ClassRect && prevSelected != matchingRect) {
                            setSelected((ClassRect)matchingRect);
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

        loadRecent();
    }

    public void loadRecent() {
        var c = cacheManager.getFirstElement(String.class, "recentFile");
        if (c != null) {
            try {
                loadSave(readJSONFileToJSON(c));
            }
            catch (Exception e) {
                System.out.println("Recent file could not be opened");
            }
        }
        else {
            System.out.println("No recent file in cache!");
        }
    }

    public static void writeToFile(String content, String filePath) {
        System.out.println("Writing to " + filePath);

        FileWriter writer = null;
        try {
            // Create a File object for the specified file path
            File file = new File(filePath);

            // Create the parent directories if they do not exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Initialize the FileWriter with the file object
            writer = new FileWriter(file);
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
        JMenu testMenu = new JMenu("Test");

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
                    loadSave(readJSONFileToJSON(savesPath + "/test" + "." + saveFormat));
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
                writeJSONToFile(createSave(), savesPath + "/test" + "." + saveFormat);
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
                    var file = chooseJavaFile(savesPath, saveFormat);
                    if (file != null) {
                        loadSave(readJSONFileToJSON(file));
                        cacheManager.addToBuffer("filesOpened", file.getPath());
                        cacheManager.addToBuffer("recentFile", file.getPath());
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
                var file = saveJavaFile(savesPath, saveFormat, "save");
                if (file != null) {
                    writeJSONToFile(createSave(), file.getPath());
                    System.out.println("File " + file.getName() + " saved!");
                }
            }
        });

        fileMenu.add(saveFileDotDotDot);

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

    public static void loadSave(JSONObject o) {
        JSONArray a2 = o.getJSONArray("data");
        dataPanel.fromJson(a2);
        JSONArray a = o.getJSONArray("rects");
        subFrame.leftPanel.fromJson(a);
        actionHandler.reset();
    }

    public static JSONObject createSave() {
        JSONObject o = new JSONObject();
        o.put("rects", subFrame.leftPanel.toJson());
        o.put("data", dataPanel.toJson());
        return o;
    }

    public <T extends JScrollPane> void addKeyListener(T j) {
        j.addKeyListener(new KeyAdapter() {

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
                else if (e.getKeyCode() == KeyEvent.VK_C && isControlPressed && selected != null) {
                    clipBoard = (ClassRect) selected;
                    revalidate();
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_V && isControlPressed && clipBoard != null) {
                    Point releasePoint = MouseInfo.getPointerInfo().getLocation();
                    Point pointFromLeft = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(pointFromLeft, leftPanel);
                    SwingUtilities.convertPointFromScreen(releasePoint, leftPanel.getViewport().getView());

                    if (leftPanel.contains(pointFromLeft)) {
                        Rect matchingRect = leftPanel.getRect(releasePoint);
                        var clone = clipBoard.clone();
                        if (matchingRect instanceof RectWithRects) {
                            var res = ((RectWithRects) matchingRect).setIndex(releasePoint, clone);
                            if (res.getFirst()) {
                                res.getSecond().getFirst().addTo(leftPanel.drawingPanel);
                                actionHandler.action(new AddedRectAction((RectWithRects) res.getSecond().getFirst(), clone, res.getSecond().getSecond()));
                            }
                        }
                        else if (matchingRect == null) {

                            leftPanel.addRect(clone);
                            actionHandler.action(new AddedRectAction(null, clone, leftPanel.getRects().size()));
                        }
                        unselect();
                        setSelected((ClassRect) clone);
                    }
                    revalidate();
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_X && isControlPressed && selected != null) {
                    if (selected.parent != null) {
                        selected.parent.setIndex(selected.parentIndex, null);
                        selected.removeFrom(leftPanel.drawingPanel);
                        actionHandler.action(new DeletedRectAction(selected.parent, selected, selected.parentIndex));
                    }
                    else {
                        actionHandler.action(new DeletedRectAction(null, selected, leftPanel.getRects().indexOf(selected)));
                        leftPanel.removeRect(selected);
                    }
                    clipBoard = selected;

                    unselect();
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
        try {
            System.out.println("Saving to " + filePath);
            // Create a File object for the specified file path
            File file = new File(filePath);

            // Create the parent directories if they do not exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write the JSON data to the file
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonArray.toString(4));
                fileWriter.flush();
            }
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
    public static JSONObject readJSONFileToJSON(File file) throws IOException, org.json.JSONException {
        // Open the file using a BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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


    public static File chooseJavaFile(String path, String filter) {
        String currentDirectory = System.getProperty("user.dir");

        // Specify the starting directory
        File startingDirectory = new File(currentDirectory + path);

        // Create a JFileChooser instance with the starting directory
        JFileChooser fileChooser = new JFileChooser(startingDirectory);

        // Set up the filter to only allow .json files
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter(filter + " Files", filter);
        fileChooser.setFileFilter(jsonFilter);

        // Optionally set it to only show files with the given extension and hide others
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show the file chooser dialog
        int returnValue = fileChooser.showOpenDialog(null);

        // Check if the user selected a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public static File saveJavaFile(String path, String filter, String defaultFileName) {
        String currentDirectory = System.getProperty("user.dir");

        // Specify the starting directory
        File startingDirectory = new File(currentDirectory + path);

        // Create a JFileChooser instance with the starting directory
        JFileChooser fileChooser = new JFileChooser(startingDirectory);

        // Set up the filter to only allow specific file types
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(filter + " Files", filter);
        fileChooser.setFileFilter(fileFilter);

        // Set default file name
        fileChooser.setSelectedFile(new File(defaultFileName + "." + filter));

        // Optionally set it to only show files with the given extension and hide others
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show the save file dialog
        int returnValue = fileChooser.showSaveDialog(null);

        // Check if the user selected a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Ensure the file has the correct extension
            if (!selectedFile.getName().endsWith("." + filter)) {
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + filter);
            }

            return selectedFile;
        } else {
            return null;
        }
    }

}