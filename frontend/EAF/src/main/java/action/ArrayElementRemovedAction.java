package action;

import test.rects.Rect;
import test.rects.multi.ArrayRect;

public class ArrayElementRemovedAction extends Action {

    ArrayRect arr;
    Rect r;
    int index;

    public ArrayElementRemovedAction(ArrayRect arr, Rect r, int index) {
        this.r = r;
        this.arr = arr;
        this.index = index;
    }

    @Override
    public void execute() {
        arr.removeElement(index);
    }

    @Override
    public void reverse() {
        arr.addElement(r, index);
    }



}
