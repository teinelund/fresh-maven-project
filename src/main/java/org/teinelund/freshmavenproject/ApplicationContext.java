package org.teinelund.freshmavenproject;


public class ApplicationContext {

    private String groupid;
    private String artifactId;
    private String versionOfApplication;
    private String projectName;
    private String packageName;
    private boolean verbose;
    private Application.TypeOfApplication typeOfApplication;
    private ApplicationType applicationType;
    boolean isNoGit = false;

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
}
