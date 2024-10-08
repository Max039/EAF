package eaf.action;

import eaf.ui.panels.ErrorPane;

import java.util.ArrayList;

public class ActionHandler {

    boolean actionPerformedSince = false;

    private ArrayList<Action> futureQue = new ArrayList<>();
    private ArrayList<Action> pastQue = new ArrayList<>();

    public ActionHandler() {

    }

    public void action(Action a) {
        futureQue = new ArrayList<>();
        pastQue.add(a);
        actionPerformedSince = true;
        ErrorPane.checkForErrors();
    }

    public void ctrlZ() {
        if (!pastQue.isEmpty()) {
            var action = pastQue.get(pastQue.size() - 1);
            action.reverse();
            pastQue.remove(action);
            futureQue.add(action);
        }
        actionPerformedSince = true;
        ErrorPane.checkForErrors();
    }

    public void ctrlY() {
        if (!futureQue.isEmpty()) {
            var action = futureQue.get(futureQue.size() - 1);
            action.execute();
            futureQue.remove(action);
            pastQue.add(action);
        }
        actionPerformedSince = true;
        ErrorPane.checkForErrors();
    }

    public void reset() {
        futureQue.clear();
        pastQue.clear();
        actionPerformedSince = false;
    }

    public void saved() {
        actionPerformedSince = false;
    }

    public boolean areChangesMadeSinceSave() {
        return actionPerformedSince;
    }

    public void changesMade() {
        actionPerformedSince = true;
    }
}
