package action;

import test.DataField;
import test.Main;

public class RemovedDataAction extends Action {

    private DataField d;


    public RemovedDataAction(DataField d) {
        this.d = d;
    }

    @Override
    public void execute() {
        Main.dataPanel.removeDataField(d);
    }

    @Override
    public void reverse() {
        Main.dataPanel.addDataField(d);
    }

}
