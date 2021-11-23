package org.teinelund.freshmavenproject;

import java.util.Collection;

public class ApplicationType {

    private String name;
    private String description;
    private Collection<String> actionNames;

    public ApplicationType(String name, String description, Collection<String> actionNames) {
        this.name = name;
        this.description = description;
        this.actionNames = actionNames;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Collection<String> getActionNames() {
        return this.actionNames;
    }
}
