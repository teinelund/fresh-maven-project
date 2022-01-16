package org.teinelund.freshmavenproject.action;

public class FileAction implements Action {

    private String sourceFileName;
    private String targetFileName;
    private String pathName;

    public FileAction(String sourceFileName, String targetFileName, String pathName) {
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
        this.pathName = pathName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public String getPropertyName() {
        return pathName;
    }
}
