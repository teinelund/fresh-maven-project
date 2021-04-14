package org.teinelund.freshmavenproject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Main class
 */
public class Application {

    @Parameter(names = { "-g", "--groupId" }, description = "Maven project group id. Mandatory.", order = 0)
    private String groupid;

    @Parameter(names = { "-a", "--artifactId" }, description = "Maven project artifact id. Mandatory.", order = 1)
    private String artifactId;

    @Parameter(names = { "-vp", "--versionOfProject" }, description = "Maven project version. Optional.", order = 2)
    private String version = "1.0.0-SNAPSHOT";

    @Parameter(names = { "-n", "--name" }, description = "Maven project name. Optional. Default value is the value of artifact id.", order = 3)
    private String projectName = "";

    @Parameter(names = { "--no-git" }, description = "Don't include .gitignore and README.md files in project.", order = 4)
    private boolean git = false;

    @Parameter(names = { "-v", "--verbose" }, description = "Verbose output.", order = 50)
    private boolean verbose = false;

    @Parameter(names = { "-V", "--version" }, description = "Version of Fresh Maven Project Application.", order = 51)
    private boolean versionOfApplication = false;

    @Parameter(names = { "-h", "--help" }, help = true, order = 52)
    private boolean help = false;

    public static void main(String[] args) {
        Application application = new Application();

        JCommander jc = JCommander.newBuilder()
                .addObject(application)
                .programName("fresh-maven-project")
                .build();
        jc.parse(args);

        try {
            application.execute(args, jc);
        }
        catch(IOException e) {
            System.err.println("[ERROR]:");
            e.printStackTrace();
        }
    }

    public void execute(String[] args, JCommander jc) throws IOException {
        if (help || versionOfApplication) {
            if (help) {
                jc.usage();
            }
            else {
                System.out.println("Fresh Maven Project (c) 2021 Henrik Teinelund.");
            }
            System.exit(0);
        }

        verifyParameters();

        Path projectFolder = createProjectFolder();
    }

    void verifyParameters() {
        if (verbose) {
            System.out.println("[VERBOSE] Verify Command Line Parameters.");
        }
        if (Objects.isNull(groupid) || groupid.isBlank()) {
            System.err.println("[ERROR] Group id is mandatory.");
            System.exit(1);
        }
        if (Objects.isNull(artifactId) || artifactId.isBlank()) {
            System.err.println("[ERROR] Artifact id is mandatory.");
            System.exit(1);
        }
    }

    Path createProjectFolder() throws IOException {
        if (verbose) {
            System.out.println("[VERBOSE] Create Project Folder.");
        }
        String projectFolderName = artifactId;
        if (!Objects.isNull(projectName) && !projectName.isBlank()) {
            projectFolderName = projectName;
        }
        Path projectFolder = Path.of(SystemUtils.USER_DIR, projectFolderName);

        if (verbose) {
            System.out.println("[VERBOSE] Project Folder Path: '" + projectFolder.toString() + "'.");
        }

        FileUtils.forceMkdir(projectFolder.toFile());

        if (verbose) {
            System.out.println("[VERBOSE] Project Folder, '" + projectFolder.toString() + "', created.");
        }

        return projectFolder;
    }
}
