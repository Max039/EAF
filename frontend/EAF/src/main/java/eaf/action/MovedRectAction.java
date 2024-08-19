package eaf.action;

import eaf.Main;
import eaf.rects.Rect;
import eaf.rects.multi.RectWithRects;

public class MovedRectAction extends  Action {


    private RectWithRects parent1;
    private RectWithRects parent2;
    private Rect moved;
    private int index1;
    private int index2;

    public MovedRectAction(RectWithRects parent1, RectWithRects parent2, Rect moved, int index1, int index2) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.moved = moved;
        this.index1 = index1;
        this.index2 = index2;
    }

    @Override
    public void execute() {
        if (parent1 == null) {
            Main.mainPanel.leftPanel.getRects().remove(moved);
        }
        else {
            parent1.setIndex(index1, null);
        }

        if (parent2 == null) {
            Main.mainPanel.leftPanel.getRects().add(index2, moved);
        }
        else {
            parent2.setIndex(index2, moved);
        }
    }

    @Override
    public void reverse() {

        if (parent2 == null) {
            Main.mainPanel.leftPanel.getRects().remove(moved);
        }
        else {
            parent2.setIndex(index2, null);
        }

        if (parent1 == null) {
            Main.mainPanel.leftPanel.getRects().add(index1, moved);
        }
        else {
            parent1.setIndex(index1, moved);
        }


    }

}
