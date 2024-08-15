package action;

import test.DataField;
import test.Main;

public class AddedDataAction extends Action {

    private DataField d;


    public AddedDataAction(DataField d) {
        this.d = d;
    }

    @Override
    public void execute() {
        Main.dataPanel.addDataField(d);
    }

    @Override
    public void reverse() {
        Main.dataPanel.removeDataField(d);
    }

}
