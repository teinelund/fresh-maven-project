package org.teinelund.freshmavenproject;

public enum TypeOfApplication {
    COMMAND_LINE_APPLICATION("Stand alone Application (Command Line Application)"),
    LIBRARY("Library (Jar file)"),
    J2EE("Java Enterprise Edition Application (J2EE)");

    private String description;

    TypeOfApplication(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
