package eaf.action;

import eaf.Main;
import eaf.models.Constant;
import eaf.models.DataField;

public class AddedConstantAction extends Action {

    private Constant c;


    public AddedConstantAction(Constant c) {
        this.c = c;
    }

    @Override
    public void execute() {
        Main.constantManager.addConstant(c);
    }

    @Override
    public void reverse() {
        Main.constantManager.removeConstant(c.name);
    }

}
