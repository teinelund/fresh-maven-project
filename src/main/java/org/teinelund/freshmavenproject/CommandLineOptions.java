package org.teinelund.freshmavenproject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class CommandLineOptions {

    @Parameter(names = { "-g", "--groupId" }, description = "Maven project group id. Mandatory.", order = 0)
    private String groupid;

    @Parameter(names = { "-a", "--artifactId" }, description = "Maven project artifact id. Mandatory.", order = 1)
    private String artifactId;

    @Parameter(names = { "-vp", "--versionOfProject" }, description = "Maven project version. Optional.", order = 2)
    private String versionOfApplication = "1.0.0-SNAPSHOT";

    @Parameter(names = { "-n", "--projectName" }, description = "Maven project name. Optional. Default value is the value of artifact id.", order = 3)
    private String projectName = "";

    @Parameter(names = { "--no-git" }, description = "Don't include .gitignore and README.md files in project.", order = 4)
    private boolean noGit = false;

    @Parameter(names = { "-v", "--verbose" }, description = "Verbose output.", order = 50)
    private boolean verbose = false;

    @Parameter(names = { "-V", "--version" }, description = "Version of Fresh Maven Project Application.", order = 51)
    private boolean version = false;

    @Parameter(names = { "-h", "--help" }, help = true, order = 52)
    private boolean help = false;

    private JCommander jc;

    public void parse(String[] args) {
        this.jc = JCommander.newBuilder()
                .addObject(this)
                .programName("fresh-maven-project")
                .build();
        jc.parse(args);
    }

    public String getGroupid() {
        return groupid;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersionOfApplication() {
        return versionOfApplication;
    }

    public String getProjectName() {
        return projectName;
    }

    public boolean isNoGit() {
        return noGit;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isVersion() {
        return version;
    }

    public boolean isHelp() {
        return help;
    }

    public void usage() {
        jc.usage();
    }
}
