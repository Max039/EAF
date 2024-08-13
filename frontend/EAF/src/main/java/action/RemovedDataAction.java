package action;

import test.DataField;
import test.DragDropRectanglesWithSplitPane;
import test.rects.TextFieldRect;

public class RemovedDataAction extends Action {

    private DataField d;


    public RemovedDataAction(DataField d) {
        this.d = d;
    }

    @Override
    public void execute() {
        DragDropRectanglesWithSplitPane.dataPanel.removeDataField(d);
    }

    @Override
    public void reverse() {
        DragDropRectanglesWithSplitPane.dataPanel.addDataField(d);
    }

}
