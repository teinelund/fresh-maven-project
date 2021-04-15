package org.teinelund.freshmavenproject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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
    private String versionOfApplication = "1.0.0-SNAPSHOT";

    @Parameter(names = { "-n", "--projectName" }, description = "Maven project name. Optional. Default value is the value of artifact id.", order = 3)
    private String projectName = "";

    @Parameter(names = { "--no-git" }, description = "Don't include .gitignore and README.md files in project.", order = 4)
    private boolean git = false;

    @Parameter(names = { "-v", "--verbose" }, description = "Verbose output.", order = 50)
    private boolean verbose = false;

    @Parameter(names = { "-V", "--version" }, description = "Version of Fresh Maven Project Application.", order = 51)
    private boolean version = false;

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
        catch(Exception e) {
            printError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execute(String[] args, JCommander jc) {
        if (help || version) {
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

        String name = artifactId;
        if (!Objects.isNull(projectName) && !projectName.isBlank()) {
            name = projectName;
        }

        String packageName = groupid.replaceAll("-", "").replaceAll("_", "") +
                "." + artifactId.replaceAll("-", "").replaceAll("_", "");

        createPomFile(projectFolder, name, packageName);

        String[] folderNames = packageName.split("\\.");

        createSrcFolderWithSubFolders(projectFolder, folderNames);
    }

    void verifyParameters() {
        printVerbose("Verify Command Line Parameters.");
        if (Objects.isNull(groupid) || groupid.isBlank()) {
            printError("Group id is mandatory.");
            System.exit(1);
        }
        if (Objects.isNull(artifactId) || artifactId.isBlank()) {
            printError("Artifact id is mandatory.");
            System.exit(1);
        }
    }

    Path createProjectFolder() {
        printVerbose("Create Project Folder.");
        String projectFolderName = artifactId;
        if (!Objects.isNull(projectName) && !projectName.isBlank()) {
            projectFolderName = projectName;
        }
        Path projectFolder = Path.of(SystemUtils.USER_DIR, projectFolderName);

        printVerbose("Project Folder Path: '" + projectFolder.toString() + "'.");

        try {
            FileUtils.forceMkdir(projectFolder.toFile());
        } catch (IOException e) {
            printError("Could not create project folder.");
            System.exit(1);
        }
        printVerbose("Project Folder, '" + projectFolder.toString() + "', created.");

        return projectFolder;
    }

    void createPomFile(Path projectFolder, String name, String packageName) {

        printVerbose("Create 'pom.xml' file.");
        printVerbose("Project name: '" + name + "', packageName: '" + packageName + "'.");

        Path pomFilePath = Path.of(projectFolder.toString(), "pom.xml");

        String pomfileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>" + groupid + "</groupId>\n" +
                "    <artifactId>" + artifactId + "</artifactId>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <version>" + versionOfApplication + "</version>\n" +
                "    <name>" + name + "</name>\n" +
                "\n" +
                "    <dependencies>\n" +
                "        <dependency>\n" +
                "            <groupId>com.beust</groupId>\n" +
                "            <artifactId>jcommander</artifactId>\n" +
                "            <version>1.81</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.apache.maven</groupId>\n" +
                "            <artifactId>maven-model</artifactId>\n" +
                "            <version>3.8.1</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.fusesource.jansi</groupId>\n" +
                "            <artifactId>jansi</artifactId>\n" +
                "            <version>2.3.2</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>commons-io</groupId>\n" +
                "            <artifactId>commons-io</artifactId>\n" +
                "            <version>2.8.0</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.apache.commons</groupId>\n" +
                "            <artifactId>commons-lang3</artifactId>\n" +
                "            <version>3.11</version>\n" +
                "        </dependency>\n" +
                "        <!-- TEST -->\n" +
                "        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter-api</artifactId>\n" +
                "            <version>5.7.1</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.junit.jupiter</groupId>\n" +
                "            <artifactId>junit-jupiter-engine</artifactId>\n" +
                "            <version>5.7.1</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.assertj</groupId>\n" +
                "            <artifactId>assertj-core</artifactId>\n" +
                "            <!-- use 2.9.1 for Java 7 projects -->\n" +
                "            <version>3.19.0</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.mockito</groupId>\n" +
                "            <artifactId>mockito-core</artifactId>\n" +
                "            <version>3.9.0</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-jar-plugin</artifactId>\n" +
                "                <version>3.2.0</version>\n" +
                "                <configuration>\n" +
                "                    <finalName>${project.name}</finalName>\n" +
                "                    <archive>\n" +
                "                        <manifest>\n" +
                "                            <mainClass>" + packageName + ".Application</mainClass>\n" +
                "                        </manifest>\n" +
                "                    </archive>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-shade-plugin</artifactId>\n" +
                "                <version>3.2.4</version>\n" +
                "                <executions>\n" +
                "                    <execution>\n" +
                "                        <phase>package</phase>\n" +
                "                        <goals>\n" +
                "                            <goal>shade</goal>\n" +
                "                        </goals>\n" +
                "                        <configuration>\n" +
                "                            <transformers>\n" +
                "                                <transformer\n" +
                "                                        implementation=\"org.apache.maven.plugins.shade.resource.ManifestResourceTransformer\">\n" +
                "                                    <!-- Main class -->\n" +
                "                                    <mainClass>" + packageName + ".Application</mainClass>\n" +
                "                                </transformer>\n" +
                "                            </transformers>\n" +
                "                        </configuration>\n" +
                "                    </execution>\n" +
                "                </executions>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.8.1</version>\n" +
                "                <configuration>\n" +
                "                    <source>14</source>\n" +
                "                    <target>14</target>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                "                <version>2.22.2</version>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-help-plugin</artifactId>\n" +
                "                <version>3.2.0</version>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-clean-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-install-plugin</artifactId>\n" +
                "                <version>2.5.2</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "\n" +
                "    </build>\n" +
                "\n" +
                "</project>";
        try {
            FileUtils.write(pomFilePath.toFile(), pomfileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            printError("Could not create pom.xml file.");
            System.exit(1);
        }
    }

    void createSrcFolderWithSubFolders(Path projectFolder, String[] folderNames) {
        printVerbose("Create 'src' folder with sub folders.");
        printVerbose("Folder names: " + Arrays.toString(folderNames) + ".");
    }

    static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    void printVerbose(String message) {
        if (verbose) {
            System.out.println("[VERBOSE] " + message);
        }
    }
}
