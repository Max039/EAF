package test;

import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import compiler.SyntaxTree;
import test.rects.*;
import test.rects.multi.ArrayRect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class DragDropRectanglesWithSplitPane extends JPanel {

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();
    public Rect draggedRect = null;
    private Point dragOffset = null;

    public JSplitPane splitPane = null;

    public static DragDropRectanglesWithSplitPane subFrame = null;

    public static JFrame mainFrame = null;

    public static boolean showButtons = false;

    // Declare the text field
    private JTextField rightPanelTextField;
    private JPanel rightContainerPanel;

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
                    names[i] = Integer.toString(i);
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
                    var r = new RectWithColorAndTextBox(RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c);
                    r.setTextBox(value.value);
                    return (T) r;
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

                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz,  ctype, 3, type.primitive);
            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    return (T) new RectWithColorAndTextBox(RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c);
                }
                else {
                    return null;
                }
            }
        }
    }

    public void setPosOfDraggingRect(MouseEvent e) {
        Point rightPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
        Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
        int newX = rightPanelPos.x - dragOffset.x;
        int newY = rightPanelPos.y - dragOffset.y;
        leftPanel.draggingRect.setPosition(newX + leftPanel.getWidth() + splitPane.getDividerSize(), leftPanelPos.y  - dragOffset.y);
        rightPanel.draggingRect.setPosition(newX, newY);
    }

    public void setDraggingRect(Rect rect, MouseEvent e, Point offset) {
        draggedRect = rect.clone();
        dragOffset = new Point(offset.x, offset.y);
        rightPanel.setDraggingRect(draggedRect);
        leftPanel.setDraggingRect(draggedRect.clone());
        setPosOfDraggingRect(e);
    }

    public void filterChanged() {
        rightPanel.filter = rightPanelTextField.getText();
        rightPanel.getVerticalScrollBar().setValue(0);
        revalidate();
        repaint();
    }

    public DragDropRectanglesWithSplitPane(int numRects) {
        setLayout(new BorderLayout());

        // Initialize the text field
        rightPanelTextField = new JTextField();

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
        rightContainerPanel.add(rightPanelTextField, BorderLayout.NORTH);
        rightContainerPanel.add(rightPanel, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightContainerPanel);
        splitPane.setDividerLocation(400); // Initial divider location
        splitPane.setResizeWeight(0.5); // Evenly split the panels
        add(splitPane, BorderLayout.CENTER);

        for (var c : SyntaxTree.getNonAbstractClasses()) {
            rightPanel.addRect(getRectFromClassType(c));
        }

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                rightPanel.requestFocusInWindow();

                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());

                Rect rect = rightPanel.getRect(point);

                if (rect != null && rect.contains(point)) {
                    setDraggingRect(rect, e, new Point(point.x - rect.getX(), point.y - rect.getY()));
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
                leftPanel.requestFocusInWindow();

                Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());

                // Convert the point from the source component's coordinate system to the left panel's coordinate system
                Point panelRelativePos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel);


                Rect matchingRect = leftPanel.getRect(leftPanelPos);
                if (matchingRect instanceof RectWithRects) {
                    matchingRect = ((RectWithRects) matchingRect).getSubRect(leftPanelPos);
                    if (matchingRect != null) {
                        matchingRect.onMouseClicked(e.getButton() == 1, leftPanelPos, panelRelativePos, e);

                    }
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

        rightPanel.getViewport().getView().addMouseListener(mouseAdapter);
        rightPanel.getViewport().getView().addMouseMotionListener(mouseAdapter);
        leftPanel.getViewport().getView().addMouseListener(mouseAdapter2);
        leftPanel.getViewport().getView().addMouseMotionListener(mouseAdapter2);
        addKeyListener(leftPanel);
        addKeyListener(rightPanel);
    }

    private static void createAndShowGUI(int numRects) {
        mainFrame = new JFrame("Drag and Drop Rectangles with Split Pane");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        subFrame = new DragDropRectanglesWithSplitPane(numRects);
        mainFrame.add(subFrame);
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

    }

    public <T extends JScrollPane> void addKeyListener(T j) {
        j.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    showButtons = true;
                    revalidate();
                    repaint();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
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
            SyntaxTree.start();
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
                    if (((RectWithRects)matchingRect).setIndex(releasePoint, draggedRect)) {
                        draggedRect.addTo(leftPanel.drawingPanel);
                    }
                }
                else {
                    leftPanel.addRect(leftPanel.draggingRect);
                }
            }


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
}