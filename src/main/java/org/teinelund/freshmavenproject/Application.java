package org.teinelund.freshmavenproject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.teinelund.freshmavenproject.action.AbstractAction;
import org.teinelund.freshmavenproject.action.Action;
import org.teinelund.freshmavenproject.action.ActionRepository;
import org.teinelund.freshmavenproject.action.FileAction;
import org.teinelund.freshmavenproject.action.FolderPathAction;
import org.teinelund.freshmavenproject.action.ListOfAction;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

/**
 * Main class
 */
public class Application {

    public static final String PomFileDependencyActionClassName = "org.teinelund.freshmavenproject.action.PomFileDependencyAction";
    public static final String PomFilePropertyActionClassName = "org.teinelund.freshmavenproject.action.PomFilePropertyAction";
    public static final String PomFilePluginActionClassName = "org.teinelund.freshmavenproject.action.PomFilePluginAction";

    public static void main(String[] args) {
        Application application = new Application();
        CommandLineOptions options = new CommandLineOptions();
        ApplicationUtils applicationUtils = new ApplicationUtils();

        ApplicationTypes applicationTypes = new ApplicationTypes();
        ActionRepository actionRepository = new ActionRepository();
        ApplicationContext applicationContext = new ApplicationContext();
        InteractiveQueryEngine interactiveQueryEngine = new InteractiveQueryEngine();

        try {
            application.execute(args, options, applicationTypes, actionRepository, applicationContext, applicationUtils, interactiveQueryEngine);
        }
        catch(Exception e) {
            applicationUtils.printError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execute(String[] args, CommandLineOptions options,
                        ApplicationTypes applicationTypes, ActionRepository actionRepository,
                        ApplicationContext applicationContext, ApplicationUtils applicationUtils,
                        InteractiveQueryEngine interactiveQueryEngine) {

        parseCommandLineOptions(args, options);

        ifHelpOptionOrVersionOption(options, applicationUtils);

        applicationUtils.printVerbose("Verbose mode on.", options);

        verifyParameters(options, applicationUtils);

        interactiveMode(options, applicationTypes, applicationContext, interactiveQueryEngine, applicationUtils);
        nonInteractiveMode(options, applicationUtils);

        createProjectFolder(applicationContext, applicationUtils);

        Context velocityContext = initializeVelocity(applicationContext, actionRepository, applicationUtils);

        // This creates:
        // * src/main/java
        // * src/test/java
        // * optional others as well ...
        createFoldersFromActionList(applicationContext, velocityContext, applicationUtils, actionRepository);

        createPackageFolders(applicationContext, velocityContext, applicationUtils, actionRepository);

        createPomFile(applicationContext, velocityContext, applicationUtils);

        createFilesFromActionList(applicationContext, velocityContext, applicationUtils, actionRepository);

        createGitFiles(applicationContext, velocityContext, applicationUtils);
    }

    void createFilesFromActionList(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils, ActionRepository actionRepository) {
        applicationUtils.printVerbose("Create files:", applicationContext);
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            createFileFromAction(action, applicationContext, velocityContext, applicationUtils);
        }
    }

    void createFileFromAction(Action action, ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                createFileFromAction(action1, applicationContext, velocityContext, applicationUtils);
            }
        }
        else if (action instanceof FileAction) {
            applicationUtils.printVerbose("    Action is a FolderPathAction", applicationContext);
            FileAction fileAction = (FileAction) action;
            processVelocityTemplate(fileAction.getTargetFileName(), fileAction.getSourceFileName(), (Path) applicationContext.getContext(fileAction.getPropertyName()),
                    applicationContext, velocityContext, applicationUtils);
        }
    }

    void createPackageFolders(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils, ActionRepository actionRepository) {
        applicationUtils.printVerbose("Create package folders:", applicationContext);
        Path path = Paths.get(((Path) applicationContext.getContext("srcMainJavaFolderNamePath")).toString(), applicationContext.getPackageFolderPathName());
        try {
            FileUtils.forceMkdir(path.toFile());
            applicationContext.putContext( "mainPackageFolderPath", path);
            applicationUtils.printVerbose("    Folder structure: '" + path.toString() + "' created.", applicationContext);
        } catch (IOException e) {
            applicationUtils.printError("Could not create folder '" + path.toString() + "'.");
            System.exit(1);
        }
        path = Paths.get(((Path) applicationContext.getContext("srcTestJavaFolderNamePath")).toString(), applicationContext.getPackageFolderPathName());
        try {
            FileUtils.forceMkdir(path.toFile());
            applicationContext.putContext( "testPackageFolderPath", path);
            applicationUtils.printVerbose("    Folder structure: '" + path.toString() + "' created.", applicationContext);
        } catch (IOException e) {
            applicationUtils.printError("Could not create folder '" + path.toString() + "'.");
            System.exit(1);
        }
    }

    void createFoldersFromActionList(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils, ActionRepository actionRepository) {
        applicationUtils.printVerbose("Create folders:", applicationContext);
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            createFoldersFromAction(action, applicationContext, velocityContext, applicationUtils);
        }
    }

    void createFoldersFromAction(Action action, ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                createFoldersFromAction(action1, applicationContext, velocityContext, applicationUtils);
            }
        }
        else if (action instanceof FolderPathAction) {
            applicationUtils.printVerbose("    Action is a FolderPathAction", applicationContext);
            FolderPathAction folderPathAction = (FolderPathAction) action;
            String[] folderPathNameArray = folderPathAction.getContent().split("/");
            Path folderPath = Path.of(applicationContext.getProjectFolder().toString(), folderPathNameArray);
            try {
                FileUtils.forceMkdir(folderPath.toFile());
            } catch (IOException e) {
                applicationUtils.printError("Could not create folder '" + folderPath.toString() + "'.");
                System.exit(1);
            }
            applicationUtils.printVerbose("    Folder structure: '" + folderPath.toString() + "' created.", applicationContext);
            velocityContext.put( folderPathAction.getPropertyName(), folderPathAction.getContent());
            applicationContext.putContext( folderPathAction.getPropertyName() + "Path", folderPath);
        }
    }

    void interactiveMode(CommandLineOptions options, ApplicationTypes applicationTypes, ApplicationContext context,
                         InteractiveQueryEngine interactiveQueryEngine, ApplicationUtils applicationUtils) {
        if (options.isInteractive()) {
            interactiveQueryEngine.userQueries(options, applicationTypes, context, applicationUtils);
        }
    }

    void nonInteractiveMode(CommandLineOptions options, ApplicationUtils applicationUtils) {
        if (!options.isInteractive()) {
            applicationUtils.printInteractive("Non interactive mode not supported, yet.");
            System.exit(1);
        }
    }

    enum TypeOfApplication {
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

    VelocityContext initializeVelocity(ApplicationContext applicationContext, ActionRepository actionRepository, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Method initializeVelocity:", applicationContext);
        String dependencies = extractApplicationTypeContent(applicationContext, actionRepository, PomFileDependencyActionClassName, applicationUtils);
        String properties = extractApplicationTypeContent(applicationContext, actionRepository, PomFilePropertyActionClassName, applicationUtils);
        String plugins = extractApplicationTypeContent(applicationContext, actionRepository, PomFilePluginActionClassName, applicationUtils);
        if (Objects.nonNull(properties) && StringUtils.isNotBlank(properties)) {
            properties = "    <properties>\n" + properties + "    </properties>\n";
        }
        applicationUtils.printVerbose("  projectName:" + applicationContext.getProjectName(), applicationContext);
        applicationUtils.printVerbose("  programNameUsedInPrintVersion:" + applicationContext.getProgramNameUsedInPrintVersion(), applicationContext);
        applicationUtils.printVerbose("  packageName:" + applicationContext.getPackageName(), applicationContext);
        applicationUtils.printVerbose("  artifactId:" + applicationContext.getArtifactId(), applicationContext);
        applicationUtils.printVerbose("  groupId:" + applicationContext.getGroupId(), applicationContext);
        applicationUtils.printVerbose("  versionOfApplication:" + applicationContext.getVersionOfApplication(), applicationContext);
        applicationUtils.printVerbose("  dependencies:" + dependencies, applicationContext);
        applicationUtils.printVerbose("  properties:" + properties, applicationContext);
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put( "projectName", applicationContext.getProjectName());
        context.put( "programNameUsedInPrintVersion", applicationContext.getProgramNameUsedInPrintVersion());
        context.put( "packageName", applicationContext.getPackageName());
        context.put( "artifactId", applicationContext.getArtifactId());
        context.put( "groupId", applicationContext.getGroupId());
        context.put( "versionOfApplication", applicationContext.getVersionOfApplication());

        StringWriter velocityEvaluatedPlugins = new StringWriter();
        Velocity.evaluate(context, velocityEvaluatedPlugins, "plugins", plugins);

        context.put( "dependencies", dependencies);
        context.put( "properties", properties);
        context.put( "plugins", velocityEvaluatedPlugins.toString());
        return context;
    }

    String extractApplicationTypeContent(ApplicationContext applicationContext, ActionRepository actionRepository, String actionClassName, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Method extractApplicationTypeContent", applicationContext);
        StringBuilder dependencies = new StringBuilder();
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            dependencies.append(extractSpecificActionContent(action, applicationContext, actionClassName, applicationUtils));
        }
        return dependencies.toString();
    }

    String extractSpecificActionContent(Action action, ApplicationContext applicationContext, String actionClassName, ApplicationUtils applicationUtils) {
        StringBuilder dependencies = new StringBuilder();
        String className = action.getClass().getName();
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                dependencies.append(extractSpecificActionContent(action1, applicationContext, actionClassName, applicationUtils));
            }
        }
        else if (className.equals(actionClassName)) {
            applicationUtils.printVerbose("    Action is a " + actionClassName, applicationContext);
            AbstractAction dependencyAction = (AbstractAction) action;
            dependencies.append(dependencyAction.getContent());
        }
        return dependencies.toString();
    }

    void parseCommandLineOptions(String[] args, CommandLineOptions options) {
        options.parse(args);
    }

    void ifHelpOptionOrVersionOption(CommandLineOptions options, ApplicationUtils applicationUtils) {
        if (options.isHelp() || options.isVersion()) {
            if (options.isHelp()) {
                options.printHelp();
            }
            else {
                options.printVersion();
            }
            applicationUtils.exit();
        }
    }

    void verifyParameters(CommandLineOptions options, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Verify Command Line Parameters.", options);

        if (!options.isInteractive()) {
            if (Objects.isNull(options.getGroupid()) || options.getGroupid().isBlank()) {
                applicationUtils.printError("Group id is mandatory.");
                applicationUtils.exitError();
            }
            if (Objects.isNull(options.getArtifactId()) || options.getArtifactId().isBlank()) {
                applicationUtils.printError("Artifact id is mandatory.");
                applicationUtils.exitError();
            }
        }
    }

    void createProjectFolder(ApplicationContext context, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Create Project Folder.", context);
        String projectFolderName = context.getArtifactId();
        if (StringUtils.isNotBlank(context.getProjectName())) {
            projectFolderName = context.getProjectName();
        }
        Path projectFolder = Path.of(context.getUserDir(), projectFolderName);

        applicationUtils.printVerbose("Project Folder Path: '" + projectFolder.toString() + "'.", context);

        try {
            FileUtils.forceMkdir(projectFolder.toFile());
        } catch (IOException e) {
            applicationUtils.printError("Could not create project folder.");
            System.exit(1);
        }
        applicationUtils.printVerbose("Project Folder, '" + projectFolder.toString() + "', created.", context);

        context.setProjectFolder(projectFolder);
    }

    void createPomFile(ApplicationContext applicationContext, Context context, ApplicationUtils applicationUtils) {

        processVelocityTemplate("pom.xml", "pom.vtl", applicationContext.getProjectFolder(),
                applicationContext, context, applicationUtils);
    }

    void processVelocityTemplate(String targetFileName, String templateName, Path targetPath, ApplicationContext applicationContext,
                                 Context context, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Create '" + targetFileName + "' source file.", applicationContext);

        Path targetFilePath = Path.of(targetPath.toString(), targetFileName);

        Template template = null;

        try
        {
            template = Velocity.getTemplate("templates/" + templateName);
        } catch( Exception e ) {
            e.printStackTrace();
            applicationUtils.exitError();
        }
        try {
            StringWriter targetFileContent = new StringWriter();
            template.merge(context, targetFileContent);
            FileUtils.write(targetFilePath.toFile(), targetFileContent.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            applicationUtils.printError("Could not create '" + targetFileName + "' file.");
            applicationUtils.exitError();
        }
    }

    void createGitFiles(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Create git files.", applicationContext);

        if (applicationContext.isNoGit()) {
            applicationUtils.printVerbose("--no-git option true. No 'README.md' or '.gitignore' files will be created for the project.", applicationContext);
            return;
        }

        processVelocityTemplate("README.md", "README.vtl", applicationContext.getProjectFolder(),
                applicationContext, velocityContext, applicationUtils);

        processVelocityTemplate(".gitignore", "gitignore.vtl", applicationContext.getProjectFolder(),
                applicationContext, velocityContext, applicationUtils);
    }
}
