package org.teinelund.freshmavenproject.action;

public class FolderPathAction implements Action {

    private String folderPath;

    public FolderPathAction(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }
}
