package action;

import test.Main;
import test.rects.Rect;
import test.rects.multi.RectWithRects;

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
            Main.subFrame.leftPanel.removeRect(deleted);
        }
        else {
            parent.setIndex(index, null);
            deleted.removeFrom(Main.subFrame.leftPanel.drawingPanel);
        }
    }

    @Override
    public void reverse() {
        if (parent == null) {
            Main.subFrame.leftPanel.getRects().add(index, deleted);
            deleted.addTo(Main.subFrame.leftPanel.drawingPanel);
        }
        else {
            parent.setIndex(index, deleted);
            deleted.addTo(Main.subFrame.leftPanel.drawingPanel);
        }
    }

}
