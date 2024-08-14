package action;

import test.DataFieldListPane;
import test.DragDropRectanglesWithSplitPane;
import test.rects.Rect;
import test.rects.TextFieldRect;
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
            DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(deleted);
        }
        else {
            parent.setIndex(index, null);
            deleted.removeFrom(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
    }

    @Override
    public void reverse() {
        if (parent == null) {
            DragDropRectanglesWithSplitPane.subFrame.leftPanel.getRects().add(index, deleted);
            deleted.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
        else {
            parent.setIndex(index, deleted);
            deleted.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
    }

}
