package org.teinelund.freshmavenproject.action;

public class PomFilePluginAction implements Action {

    private String pluginContent;

    public PomFilePluginAction(String pluginContent) {
        this.pluginContent = pluginContent;
    }

    public String getPluginContent() {
        return pluginContent;
    }
}
