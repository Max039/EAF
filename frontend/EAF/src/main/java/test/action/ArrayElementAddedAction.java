package test.action;

import test.rects.Rect;
import test.rects.multi.ArrayRect;

public class ArrayElementAddedAction extends Action {

    ArrayRect arr;
    Rect r;

    public ArrayElementAddedAction(ArrayRect arr, Rect r) {
        this.r = r;
        this.arr = arr;
    }

    @Override
    public void execute() {
        arr.addLast(r);
    }

    @Override
    public void reverse() {
        arr.removeLast();
    }



}
