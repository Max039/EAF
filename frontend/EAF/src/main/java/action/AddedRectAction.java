package action;

import test.DataFieldListPane;
import test.DragDropRectanglesWithSplitPane;
import test.rects.Rect;
import test.rects.TextFieldRect;
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
            DragDropRectanglesWithSplitPane.subFrame.leftPanel.getRects().add(index, added);
            added.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
        else {
            parent.setIndex(index, added);
            added.addTo(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
    }

    @Override
    public void reverse() {
        if (parent == null) {
            DragDropRectanglesWithSplitPane.subFrame.leftPanel.removeRect(added);
        }
        else {
            parent.setIndex(index, null);
            added.removeFrom(DragDropRectanglesWithSplitPane.subFrame.leftPanel.drawingPanel);
        }
    }

}
