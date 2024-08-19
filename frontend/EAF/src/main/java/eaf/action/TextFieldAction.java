package eaf.action;

import eaf.rects.TextFieldRect;

public class TextFieldAction extends Action {

    private TextFieldRect r;
    private String oldContent;
    private String newContent;

    public TextFieldAction(TextFieldRect r, String oldContent, String newContent) {
        this.r = r;
        this.newContent = newContent;
        this.oldContent = oldContent;
    }

    @Override
    public void execute() {
        r.textBox.setText(newContent);
    }

    @Override
    public void reverse() {
        r.textBox.setText(oldContent);
    }
}
