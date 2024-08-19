package test.action;

import test.Main;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

public class AddedRectAction extends  Action {
    private RectWithRects parent;
    private Rect added;
    private int index;

    public AddedRectAction(RectWithRects parent, Rect added, int index) {
        this.parent = parent;
        this.added = added;
        this.index = index;
    }

    @Override
    public void execute() {
        if (parent == null) {
            Main.mainPanel.leftPanel.getRects().add(index, added);
            added.addTo(Main.mainPanel.leftPanel.drawingPanel);
        }
        else {
            parent.setIndex(index, added);
            added.addTo(Main.mainPanel.leftPanel.drawingPanel);
        }
    }

    @Override
    public void reverse() {
        if (parent == null) {
            Main.mainPanel.leftPanel.removeRect(added);
        }
        else {
            parent.setIndex(index, null);
            added.removeFrom(Main.mainPanel.leftPanel.drawingPanel);
        }
    }

}
