package action;

import test.DataField;
import test.DragDropRectanglesWithSplitPane;
import test.rects.TextFieldRect;

public class AddedDataAction extends Action {

    private DataField d;


    public AddedDataAction(DataField d) {
        this.d = d;
    }

    @Override
    public void execute() {
        DragDropRectanglesWithSplitPane.dataPanel.addDataField(d);
    }

    @Override
    public void reverse() {
        DragDropRectanglesWithSplitPane.dataPanel.removeDataField(d);
    }

}
