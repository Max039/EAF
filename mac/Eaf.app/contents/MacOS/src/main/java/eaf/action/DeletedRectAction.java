package eaf.action;

import eaf.Main;
import eaf.rects.Rect;
import eaf.rects.multi.RectWithRects;

public class DeletedRectAction extends  Action {

    private RectWithRects parent;
    private Rect deleted;
    private int index;


    public DeletedRectAction(RectWithRects parent, Rect deleted, int index) {
        this.parent = parent;
        this.deleted = deleted;
        this.index = index;
    }

    @Override
    public void execute() {
        if (parent == null) {
            Main.mainPanel.leftPanel.removeRect(deleted);
        }
        else {
            parent.setIndex(index, null);
            deleted.removeFrom(Main.mainPanel.leftPanel.drawingPanel);
        }
    }

    @Override
    public void reverse() {
        if (parent == null) {
            Main.mainPanel.leftPanel.getRects().add(index, deleted);
            deleted.addTo(Main.mainPanel.leftPanel.drawingPanel);
        }
        else {
            parent.setIndex(index, deleted);
            deleted.addTo(Main.mainPanel.leftPanel.drawingPanel);
        }
    }

}
