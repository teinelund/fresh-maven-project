package org.teinelund.freshmavenproject.action;

public class FolderPathAction extends AbstractAction {

    private String propertyName;

    public FolderPathAction(String folderPath, String propertyName) {
        super(folderPath);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return "[folderPath: " + this.content + ", propertyName: " + this.propertyName + "]";
    }
}
