package test;

import compiler.SyntaxTree;
import test.rects.*;
import test.rects.multi.ArrayRect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

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

    public void setPosOfDraggingRect(MouseEvent e) {
        Point rightPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
        Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
        int newX = rightPanelPos.x - dragOffset.x;
        int newY = rightPanelPos.y - dragOffset.y;
        leftPanel.draggingRect.setPosition(newX + leftPanel.getWidth() + splitPane.getDividerSize(), leftPanelPos.y  - dragOffset.y);
        rightPanel.draggingRect.setPosition(newX, newY);
    }

    public DragDropRectanglesWithSplitPane(int numRects) {
        setLayout(new BorderLayout());




        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400); // Initial divider location
        splitPane.setResizeWeight(0.5); // Evenly split the panels
        add(splitPane, BorderLayout.CENTER);

        /**
        Random random = new Random();
        for (int i = 0; i < numRects; i++) {
            int width = random.nextInt(100) + 50;
            int height = random.nextInt(50) + 30;
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Rect r;

            if (i == 1) {
                r = new RectWithColorAndTextBox(width, height, color);

            } else if (i == 2) {
                r = new ClassRect(width, height, color, new String[]{"11111", "2222222222"}, new Class<?>[]{RectWithColorAndTextBox.class, Rect.class});
            } else if (i == 3) {
                r = new ArrayRect<RectWithColorAndTextBox>(width, height, color, 3, RectWithColorAndTextBox.class, true);
            }else if (i == 4) {
                r = new ArrayRect<RectWithRects>(width, height, color, 3, RectWithRects.class, false);
            }
            else {
                r = RectWithColor.createRectWithColor(width, height, color);
            }
            rightPanel.addRect(r);
        }
         **/
        for (var c : SyntaxTree.classRegister.entrySet()) {
            if (!c.getValue().isAbstract ) {
                rightPanel.addRect(RectPanel.getRectFromClassType(c.getValue()));
            }
        }



        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                rightPanel.requestFocusInWindow();

                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());

                Rect rect = rightPanel.getRect(point);

                if (rect != null && rect.contains(point)) {
                    draggedRect = rect.clone();
                    dragOffset = new Point(point.x - rect.getX(), point.y - rect.getY());
                    rightPanel.setDraggingRect(draggedRect);
                    leftPanel.setDraggingRect(draggedRect.clone());
                    setPosOfDraggingRect(e);

                }
            }

            @Override
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

            @Override
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


        };

        MouseAdapter mouseAdapter2 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                leftPanel.requestFocusInWindow();

            }

            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

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
}