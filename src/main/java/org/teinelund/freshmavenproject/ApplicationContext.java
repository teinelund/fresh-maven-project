package org.teinelund.freshmavenproject;


import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext implements Verbosable {

    private String groupid;
    private String artifactId;
    private String versionOfApplication;
    private String projectName;
    private String packageName;
    private String packageFolderPathName;
    private boolean verbose;
    private TypeOfApplication typeOfApplication;
    private ApplicationType applicationType;
    boolean isNoGit = false;
    private String programNameUsedInPrintVersion;
    private Path srcMainJavaPackagePath;
    private Path srcTestJavaPackagePath;

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

    public void setTypeOfApplication(TypeOfApplication typeOfApplication) {
        this.typeOfApplication = typeOfApplication;
    }

    public TypeOfApplication getTypeOfApplication() {
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

    public void setPackageFolderPathName(String packageFolderPathName) {
        this.packageFolderPathName = packageFolderPathName;
    }

    public String getPackageFolderPathName() {
        return packageFolderPathName;
    }

    public void setProgrameNameUsedInPrintVersion(String programNameUsedInPrintVersion) {
        this.programNameUsedInPrintVersion = programNameUsedInPrintVersion;
    }

    public String getProgramNameUsedInPrintVersion() {
        return programNameUsedInPrintVersion;
    }

    public void setSrcMainJavaPackagePath(Path srcMainJavaPackagePath) {
        this.srcMainJavaPackagePath = srcMainJavaPackagePath;
    }

    public void setSrcTestJavaPackagePath(Path srcTestJavaPackagePath) {
        this.srcTestJavaPackagePath = srcTestJavaPackagePath;
    }

    public Path getSrcMainJavaPackagePath() {
        return this.srcMainJavaPackagePath;
    }

    public Path getSrcTestJavaPackagePath() {
        return this.srcTestJavaPackagePath;
    }

    public String getUserDir() {
        return SystemUtils.USER_DIR;
    }
}
