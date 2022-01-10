package org.teinelund.freshmavenproject.action;

public abstract class AbstractAction implements Action {

    protected String content;

    public AbstractAction(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
