package org.teinelund.freshmavenproject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * Main class
 */
public class Application {

    public static void main(String[] args) {
        Application application = new Application();
        CommandLineOptions options = new CommandLineOptions();

        try {
            application.execute(args, options);
        }
        catch(Exception e) {
            printError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execute(String[] args, CommandLineOptions options) {

        parseCommandLineOptions(args, options);

        ifHelpOptionOrVersionOption(options);

        printVerbose("Verbose mode on.", options);

        verifyParameters(options);

        Path projectFolder = createProjectFolder(options);

        String programName = options.getArtifactId();
        String projectName = options.getProjectName();
        if (!Objects.isNull(projectName) && !projectName.isBlank()) {
            programName = projectName;
        }

        String packageName = options.getGroupid().replaceAll("-", "").replaceAll("_", "") +
                "." + options.getArtifactId().replaceAll("-", "").replaceAll("_", "");

        String versionName = createVersionName(programName);

        Context context = initializeVelocity(programName, versionName, packageName, options);

        printVerbose("Project name: '" + programName + "', packageName: '" + packageName + "'.", options);

        createPomFile(projectFolder, options, context);

        String[] folderNames = packageName.split("\\.");
        printVerbose("Folder names: " + Arrays.toString(folderNames) + ".", options);
        Path srcMainJava = Path.of(projectFolder.toString(), "src", "main", "java");
        Path srcMainJavaPackagePath = Path.of(srcMainJava.toString(), folderNames);
        Path srcTestJava = Path.of(projectFolder.toString(), "src", "test", "java");
        Path srcTestJavaPackagePath = Path.of(srcTestJava.toString(), folderNames);

        createSrcFolderWithSubFolders(projectFolder, srcMainJavaPackagePath, srcTestJavaPackagePath, options);

        createApplicationSourceFile(srcMainJavaPackagePath, options, context);

        createApplicationTestSourceFile(srcTestJavaPackagePath, options, context);

        createGitFiles(projectFolder, versionName, options, context);
    }

    VelocityContext initializeVelocity(String programName, String versionName, String packageName, CommandLineOptions options) {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put( "programName", programName);
        context.put( "versionName", versionName);
        context.put( "packageName", packageName);
        context.put( "artifactId", options.getArtifactId());
        context.put( "groupId", options.getGroupid());
        context.put( "versionOfApplication", options.getVersionOfApplication());
        return context;
    }

    void parseCommandLineOptions(String[] args, CommandLineOptions options) {
        options.parse(args);
    }

    private void ifHelpOptionOrVersionOption(CommandLineOptions options) {
        if (options.isHelp() || options.isVersion()) {
            if (options.isHelp()) {
                options.usage();
            }
            else {
                String versionString = this.getClass().getPackage().getImplementationVersion();
                System.out.println("Fresh Maven Project. Version: " + versionString);
                System.out.println("Copyright (c) 2021 Henrik Teinelund.");
            }
            System.exit(0);
        }
    }

    String createVersionName(String name) {
        String versionName = name.replaceAll("-", " ").replaceAll("_", " ");
        String versionNameArr[] = versionName.split(" ");
        for (int i=0; i<versionNameArr.length; i++) {
            versionNameArr[i] = StringUtils.capitalize(versionNameArr[i]);
        }
        versionName = String.join(" ", versionNameArr);
        return versionName;
    }

    void verifyParameters(CommandLineOptions options) {
        printVerbose("Verify Command Line Parameters.", options);
        if (Objects.isNull(options.getGroupid()) || options.getGroupid().isBlank()) {
            printError("Group id is mandatory.");
            System.exit(1);
        }
        if (Objects.isNull(options.getArtifactId()) || options.getArtifactId().isBlank()) {
            printError("Artifact id is mandatory.");
            System.exit(1);
        }
    }

    Path createProjectFolder(CommandLineOptions options) {
        printVerbose("Create Project Folder.", options);
        String projectFolderName = options.getArtifactId();
        if (!Objects.isNull(options.getProjectName()) && !options.getProjectName().isBlank()) {
            projectFolderName = options.getProjectName();
        }
        Path projectFolder = Path.of(SystemUtils.USER_DIR, projectFolderName);

        printVerbose("Project Folder Path: '" + projectFolder.toString() + "'.", options);

        try {
            FileUtils.forceMkdir(projectFolder.toFile());
        } catch (IOException e) {
            printError("Could not create project folder.");
            System.exit(1);
        }
        printVerbose("Project Folder, '" + projectFolder.toString() + "', created.", options);

        return projectFolder;
    }

    void createPomFile(Path projectFolder, CommandLineOptions options, Context context) {

        processVelocityTemplate("pom.xml", "pom.vty", projectFolder,
                options, context);
    }

    void createSrcFolderWithSubFolders(Path projectFolder, Path srcMainJavaPackagePath, Path srcTestJavaPackagePath,
                                       CommandLineOptions options) {
        printVerbose("Create 'src' folder with sub folders.", options);
        printVerbose("Java source path: '" + srcMainJavaPackagePath.toString() + "', java test path: '" +
                srcTestJavaPackagePath.toString() + "'.", options);

        try {
            FileUtils.forceMkdir(srcMainJavaPackagePath.toFile());
        } catch (IOException e) {
            printError("Could not create folder '" + srcMainJavaPackagePath.toString() + "'.");
            System.exit(1);
        }
        printVerbose("Folder structure: '" + srcMainJavaPackagePath.toString() + "' created.", options);

        try {
            FileUtils.forceMkdir(srcTestJavaPackagePath.toFile());
        } catch (IOException e) {
            printError("Could not create folder '" + srcTestJavaPackagePath.toString() + "'.");
            System.exit(1);
        }
        printVerbose("Folder structure: '" + srcTestJavaPackagePath.toString() + "' created.", options);
    }

    void processVelocityTemplate(String targetFileName, String templateName, Path targetPath, CommandLineOptions options, Context context) {
        printVerbose("Create '" + targetFileName + "' source file.", options);

        Path targetFilePath = Path.of(targetPath.toString(), targetFileName);

        Template template = null;

        try
        {
            template = Velocity.getTemplate("templates/" + templateName);
        }
        catch( ResourceNotFoundException | ParseErrorException | MethodInvocationException e )
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            StringWriter targetFileContent = new StringWriter();
            template.merge(context, targetFileContent);
            FileUtils.write(targetFilePath.toFile(), targetFileContent.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            printError("Could not create '" + targetFileName + "' file.");
            System.exit(1);
        }
    }

    void createApplicationSourceFile(Path srcMainJavaPackagePath,
                                     CommandLineOptions options, Context context) {
        processVelocityTemplate("Application.java", "Application.vty", srcMainJavaPackagePath,
                options, context);
    }

    void createApplicationTestSourceFile(Path srcTestJavaPackagePath, CommandLineOptions options, Context context) {

        processVelocityTemplate("ApplicationTest.java", "ApplicationTest.vty", srcTestJavaPackagePath,
                options, context);
    }

    void createGitFiles(Path projectFolder, String versionName, CommandLineOptions options, Context context) {
        printVerbose("Create git files.", options);

        if (options.isNoGit()) {
            printVerbose("--no-git option true. No 'README.md' or '.gitignore' files will be created for the project.", options);
            return;
        }

        processVelocityTemplate("README.md", "README.vty", projectFolder,
                options, context);

        processVelocityTemplate(".gitignore", "gitignore.vty", projectFolder,
                options, context);
    }

    static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    void printVerbose(String message, CommandLineOptions options) {
        if (options.isVerbose()) {
            System.out.println("[VERBOSE] " + message);
        }
    }
}
