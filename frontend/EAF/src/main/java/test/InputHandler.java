package test;

import action.ActionHandler;
import action.AddedRectAction;
import action.DeletedRectAction;
import action.MovedRectAction;
import test.rects.Rect;
import test.rects.multi.ClassRect;
import test.rects.multi.RectWithRects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InputHandler {
    public static boolean isControlPressed = false;
    public static boolean showButtons = false;
    public static ClassRect selected = null;
    public static ClassRect clipBoard = null;
    public static ActionHandler actionHandler = new ActionHandler();
    public static RectWithRects draggingSource = null;
    public static Rect draggedRect = null;
    public static JLabel contentLabel;
    public static JLabel contentLabel2;
    public static HashMap<String, ArrayList<Integer>> stringMarker = new HashMap<>();
    public static int current = 0;
    private static Point dragOffset = null;

    public static void setSelected(ClassRect r) {
        selected = r ;
        selected.select();
    }

    public static void unselect() {
        if (selected != null) {
            selected.unselect();
            selected = null;
        }
    }

    public static <T extends JScrollPane> void addKeyListener(T j) {
        j.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                var leftPanel = Main.mainPanel.leftPanel;
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isControlPressed = true;
                }
                else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    showButtons = true;
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Z && isControlPressed) {
                    actionHandler.ctrlZ();
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Y && isControlPressed) {
                    actionHandler.ctrlY();
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_C && isControlPressed && selected != null) {
                    clipBoard = (ClassRect) selected;
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
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
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_X && isControlPressed && selected != null) {
                    if (selected.parent != null) {
                        selected.parent.setIndex(selected.parentIndex, null);
                        selected.removeFrom(leftPanel.drawingPanel);
                        actionHandler.action(new DeletedRectAction(selected.parent, selected, selected.parentIndex));
                    }
                    else {
                        leftPanel.removeRect(selected);
                        actionHandler.action(new DeletedRectAction(null, selected, leftPanel.getRects().indexOf(selected)));

                    }
                    clipBoard = selected;

                    unselect();
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_S && isControlPressed) {
                    FileManager.save();
                }
                else if (e.getKeyCode() == KeyEvent.VK_R && isControlPressed) {
                    if (ErrorManager.errors > 0) {
                        SoundManager.playExclamationSound();
                        Main.mainPanel.leftPanel.getVerticalScrollBar().setValue(ErrorManager.first);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isControlPressed = false;
                }
                else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    showButtons = false;
                    Main.mainPanel.revalidate();
                    Main.mainPanel.repaint();
                }
            }
        });

        j.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                showButtons = false;
                Main.mainPanel.revalidate();
                Main.mainPanel.repaint();
            }
        });
    }

    public static void setPosOfDraggingRect(MouseEvent e) {
        var leftPanel = Main.mainPanel.leftPanel;
        var rightPanel = Main.mainPanel.rightPanel;
        Point rightPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), rightPanel.getViewport().getView());
        Point leftPanelPos = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
        int newX = rightPanelPos.x - dragOffset.x;
        int newY = rightPanelPos.y - dragOffset.y;
        leftPanel.draggingRect.setPosition(newX + leftPanel.getWidth() + Main.mainPanel.mainSplitPane.getDividerSize(), leftPanelPos.y  - dragOffset.y);
        rightPanel.draggingRect.setPosition(newX, newY);
    }

    public static void setDraggingRect(Rect rect, MouseEvent e, Point offset, RectWithRects source) {
        var leftPanel = Main.mainPanel.leftPanel;
        var rightPanel = Main.mainPanel.rightPanel;
        draggingSource = source;
        draggedRect = rect;
        dragOffset = new Point(offset.x, offset.y);
        rightPanel.setDraggingRect(draggedRect.clone());
        leftPanel.setDraggingRect(draggedRect);
        setPosOfDraggingRect(e);
    }

    public static void mouseDragged(MouseEvent e) {
        var leftPanel = Main.mainPanel.leftPanel;
        var rightPanel = Main.mainPanel.rightPanel;
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
            Main.mainPanel.repaint();
        }
    }

    public static void mouseReleased(MouseEvent e) {
        var leftPanel = Main.mainPanel.leftPanel;
        var rightPanel = Main.mainPanel.rightPanel;
        if (draggedRect != null) {
            Point releasePoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel.getViewport().getView());
            Point pointFromLeft = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), leftPanel);
            if (leftPanel.contains(pointFromLeft)) {
                Rect matchingRect = leftPanel.getRect(releasePoint);
                if (matchingRect instanceof RectWithRects) {
                    int p = draggedRect.parentIndex;
                    var res = ((RectWithRects)matchingRect).setIndex(releasePoint, draggedRect);
                    if (res.getFirst()) {
                        draggedRect.addTo(leftPanel.drawingPanel);
                        if (draggingSource != null) {
                            actionHandler.action(new MovedRectAction(draggingSource, (RectWithRects)res.getSecond().getFirst(), draggedRect, p, res.getSecond().getSecond()));
                        }
                        else {
                            actionHandler.action(new AddedRectAction((RectWithRects)res.getSecond().getFirst(), draggedRect, res.getSecond().getSecond()));
                        }
                    }
                    else {
                        actionHandler.action(new DeletedRectAction(draggingSource, draggedRect, p));
                    }

                }
                else if (matchingRect == null) {
                    leftPanel.addRect(leftPanel.draggingRect);
                    if (draggingSource != null) {
                        actionHandler.action(new MovedRectAction(draggingSource, null, draggedRect, draggedRect.parentIndex, leftPanel.getRects().size()));
                    }
                    else {
                        actionHandler.action(new AddedRectAction(null, draggedRect, leftPanel.getRects().size()));
                    }
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
            Main.mainPanel.revalidate();
            Main.mainPanel.repaint();
        }
    }

    public static void filterChanged() {
        var rightPanel = Main.mainPanel.rightPanel;
        rightPanel.filter = Main.mainPanel.rightPanelTextField.getText();
        rightPanel.getVerticalScrollBar().setValue(0);
        var cnt = rightPanel.getMatchingRects();
        if (cnt < 1) {
            Main.mainPanel.rightPanelTextField.setForeground(Main.searchBarError);
            SoundManager.playExclamationSound();
        }
        else {
            Main.mainPanel.rightPanelTextField.setForeground(Main.searchBarText);
        }
        contentLabel.setText(Long.toString(cnt));
        contentLabel.revalidate();
        contentLabel.repaint();
        Main.mainPanel.revalidate();
        Main.mainPanel.repaint();
    }

    public static ArrayList<Integer> getStringMarkers() {
        return new ArrayList<>(stringMarker.entrySet().stream().filter(t -> t.getKey().toLowerCase().contains(Main.mainPanel.leftPanelTextField.getText().toLowerCase())).map(Map.Entry::getValue).flatMap(Collection::stream).sorted().toList());
    }

    public static void searchChanged() {
        Main.mainPanel.leftPanel.forceAdjustRects();
        var leftPanelTextField = Main.mainPanel.leftPanelTextField;
        var res = getStringMarkers();
        if (res.isEmpty() && !leftPanelTextField.getText().isEmpty()) {
            leftPanelTextField.setForeground(Main.searchBarError);
            SoundManager.playExclamationSound();
        }
        else {
            leftPanelTextField.setForeground(Main.searchBarText);
        }
        if (leftPanelTextField.getText().isEmpty()) {
            contentLabel2.setText("");
        }
        else {
            contentLabel2.setText(current + "/" + res.size());
        }
        Main.mainPanel.revalidate();
        Main.mainPanel.repaint();
    }
}
