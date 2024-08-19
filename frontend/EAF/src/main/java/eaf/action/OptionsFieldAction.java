package eaf.action;

import eaf.rects.OptionsFieldRect;

public class OptionsFieldAction extends Action {

    private OptionsFieldRect r;
    private String oldContent;
    private String newContent;

    public OptionsFieldAction(OptionsFieldRect r, String oldContent, String newContent) {
        this.r = r;
        this.newContent = newContent;
        this.oldContent = oldContent;
    }

    @Override
    public void execute() {
        r.comboBox.setSelectedItem(newContent);
    }

    @Override
    public void reverse() {
        r.comboBox.setSelectedItem(oldContent);
    }
}
