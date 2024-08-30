package eaf.action;

import eaf.Main;
import eaf.models.Constant;

public class RemovedConstantAction extends Action {

    private Constant c;


    public RemovedConstantAction(Constant c) {
        this.c = c;
    }

    @Override
    public void execute() {
        Main.constantManager.removeConstant(c.name);
    }

    @Override
    public void reverse() {
        Main.constantManager.addConstant(c);
    }

}
