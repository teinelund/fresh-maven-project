package org.teinelund.freshmavenproject.action;

public class PomFileDependencyAction implements Action {

    private String dependencyContent;

    public PomFileDependencyAction(String dependencyContent) {
        this.dependencyContent = dependencyContent;
    }

    public String getDependencyContent() {
        return dependencyContent;
    }
}
