package org.teinelund.freshmavenproject;


import java.nio.file.Path;

public class ApplicationContext implements Verbosable {

    private String groupid;
    private String artifactId;
    private String versionOfApplication;
    private String projectName;
    private String packageName;
    private String folderPath;
    private Path projectFolder;
    private boolean verbose;
    private Application.TypeOfApplication typeOfApplication;
    private ApplicationType applicationType;
    boolean isNoGit = false;
    private String programNameUsedInPrintVersion;

    public String getGroupId() {
        return groupid;
    }

    public void setGroupId(String groupid) {
        this.groupid = groupid;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersionOfApplication() {
        return versionOfApplication;
    }

    public void setVersionOfApplication(String versionOfApplication) {
        this.versionOfApplication = versionOfApplication;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setTypeOfApplication(Application.TypeOfApplication typeOfApplication) {
        this.typeOfApplication = typeOfApplication;
    }

    public Application.TypeOfApplication getTypeOfApplication() {
        return this.typeOfApplication;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public void setNoGit(String interactiveQuery) {
        this.isNoGit = "n".equals(interactiveQuery.trim());
    }

    public boolean isNoGit() {
        return this.isNoGit;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setProjectFolder(Path projectFolder) {
        this.projectFolder = projectFolder;
    }

    public Path getProjectFolder() {
        return projectFolder;
    }

    public void setProgrameNameUsedInPrintVersion(String programNameUsedInPrintVersion) {
        this.programNameUsedInPrintVersion = programNameUsedInPrintVersion;
    }

    public String getProgramNameUsedInPrintVersion() {
        return programNameUsedInPrintVersion;
    }
}
