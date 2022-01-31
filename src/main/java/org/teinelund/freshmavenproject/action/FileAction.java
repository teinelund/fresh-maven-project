package org.teinelund.freshmavenproject.action;

public class FileAction implements Action {

    private String sourceFileName;
    private String targetFileName;
    private String propertyName;

    public FileAction(String sourceFileName, String targetFileName, String propertyName) {
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
        this.propertyName = propertyName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
