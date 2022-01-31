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
import java.util.Objects;
import java.util.Properties;

/**
 * Main class
 */
public class Application {

    public static final String PomFileDependencyActionClassName = "org.teinelund.freshmavenproject.action.PomFileDependencyAction";
    public static final String PomFilePropertyActionClassName = "org.teinelund.freshmavenproject.action.PomFilePropertyAction";
    public static final String PomFilePluginActionClassName = "org.teinelund.freshmavenproject.action.PomFilePluginAction";
    public static final String projectFolderPathPropertyName = "projectFolderPath";
    public static final String projectFolderPathNamePropertyName = "projectFolderPathName";

    public static void main(String[] args) {
        Application application = new Application();
        CommandLineOptions options = new CommandLineOptions();
        ApplicationUtils applicationUtils = new ApplicationUtils();

        ApplicationTypes applicationTypes = new ApplicationTypes();
        ActionRepository actionRepository = new ActionRepository();
        ApplicationContext applicationContext = new ApplicationContext();
        InteractiveQueryEngine interactiveQueryEngine = new InteractiveQueryEngine();
        PropertyRepository propertyRepository = new PropertyRepository();

        try {
            application.execute(args, options, applicationTypes, actionRepository, applicationContext, applicationUtils, interactiveQueryEngine, propertyRepository);
        }
        catch(Exception e) {
            applicationUtils.printError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execute(String[] args, CommandLineOptions options,
                        ApplicationTypes applicationTypes, ActionRepository actionRepository,
                        ApplicationContext applicationContext, ApplicationUtils applicationUtils,
                        InteractiveQueryEngine interactiveQueryEngine, PropertyRepository propertyRepository) {

        parseCommandLineOptions(args, options);

        ifHelpOptionOrVersionOption(options, applicationUtils);

        applicationUtils.printVerbose("Verbose mode on.", options);

        verifyParameters(options, applicationUtils);

        interactiveMode(options, applicationTypes, applicationContext, interactiveQueryEngine, applicationUtils);
        nonInteractiveMode(options, applicationUtils);

        initializeVelocity(applicationContext, applicationUtils);

        Context velocityContext = initializeVelocityContext(applicationContext, actionRepository, propertyRepository, applicationUtils);

        createProjectFolder(applicationContext, applicationUtils, propertyRepository);

        addFolderPropertiesToVelocityContext(velocityContext, applicationContext, actionRepository, propertyRepository, applicationUtils);

        // This creates:
        // * src/main/java
        // * src/test/java
        // * optional others as well ...
        createFoldersFromActionList(applicationContext, applicationUtils, actionRepository, propertyRepository);

        createFilesFromActionList(applicationContext, velocityContext, applicationUtils, actionRepository, propertyRepository);

        createGitFiles(applicationContext, velocityContext, applicationUtils, propertyRepository);
    }

    void addFolderPropertiesToVelocityContext(Context velocityContext, ApplicationContext applicationContext, ActionRepository actionRepository, PropertyRepository propertyRepository, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Method addFolderPropertiesToVelocityContext:", applicationContext);
        UNRESOLVED_PROPERTIES unresolvedProperties;
        int scanNr = 0;
        do {
            unresolvedProperties = UNRESOLVED_PROPERTIES.NO;
            for (String actionName : applicationContext.getApplicationType().getActionNames()) {
                applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
                Action action = actionRepository.getAction(actionName);
                UNRESOLVED_PROPERTIES unresolvedProperties1 = addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
                if (unresolvedProperties1 == UNRESOLVED_PROPERTIES.YES) {
                    unresolvedProperties = UNRESOLVED_PROPERTIES.YES;
                }
            }
            if (unresolvedProperties == UNRESOLVED_PROPERTIES.YES) {
                applicationUtils.printVerbose("Unresolved properties. Rescan...", applicationContext);
                scanNr++;
                if (scanNr == 10) {
                    throw new RuntimeException("Unresolved properties.");
                }
            }
        } while (unresolvedProperties == UNRESOLVED_PROPERTIES.YES);
    }

    enum UNRESOLVED_PROPERTIES {YES, NO};

    UNRESOLVED_PROPERTIES addFolderPropertiesToVelocityContextFromFolderPathAction(Action action, ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils, PropertyRepository propertyRepository) {
        UNRESOLVED_PROPERTIES unresolvedProperties = UNRESOLVED_PROPERTIES.NO;
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                UNRESOLVED_PROPERTIES unresolvedProperties1 = addFolderPropertiesToVelocityContextFromFolderPathAction(action1, applicationContext, velocityContext, applicationUtils, propertyRepository);
                if (unresolvedProperties1 == UNRESOLVED_PROPERTIES.YES) {
                    unresolvedProperties = UNRESOLVED_PROPERTIES.YES;
                }
            }
        }
        else if (action instanceof FolderPathAction) {
            FolderPathAction folderPathAction = (FolderPathAction) action;
            applicationUtils.printVerbose("    Action is a FolderPathAction: " + folderPathAction + ".", applicationContext);
            String propertyName = folderPathAction.getPropertyName();
            boolean propertyNotExist = propertyRepository.containsNotProperty(propertyName);
            if (propertyNotExist) {
                StringWriter velocityEvaluatedFolderContent = new StringWriter();
                Velocity.evaluate(velocityContext, velocityEvaluatedFolderContent, "folderContent", folderPathAction.getContent());
                if (velocityEvaluatedFolderContent.toString().contains("${")) {
                    unresolvedProperties = UNRESOLVED_PROPERTIES.YES;
                    applicationUtils.printVerbose("  Unresolved property: " + propertyName + ", content: '" + velocityEvaluatedFolderContent.toString() + "'.", applicationContext);
                }
                else {
                    velocityContext.put(folderPathAction.getPropertyName(), velocityEvaluatedFolderContent.toString());
                    propertyRepository.put(folderPathAction.getPropertyName(), velocityEvaluatedFolderContent.toString());
                }
            }
        }
        return unresolvedProperties;
    }



    Context initializeVelocityContext(ApplicationContext applicationContext, ActionRepository actionRepository, PropertyRepository propertyRepository, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Initialize VelocityContext:", applicationContext);

        VelocityContext context = new VelocityContext();

        context.put( "projectName", applicationContext.getProjectName());
        propertyRepository.put("projectName", applicationContext.getProjectName());
        applicationUtils.printVerbose("  projectName:" + applicationContext.getProjectName(), applicationContext);
        context.put( "programNameUsedInPrintVersion", applicationContext.getProgramNameUsedInPrintVersion());
        propertyRepository.put( "programNameUsedInPrintVersion", applicationContext.getProgramNameUsedInPrintVersion());
        applicationUtils.printVerbose("  programNameUsedInPrintVersion:" + applicationContext.getProgramNameUsedInPrintVersion(), applicationContext);
        context.put( "packageName", applicationContext.getPackageName());
        propertyRepository.put( "packageName", applicationContext.getPackageName());
        applicationUtils.printVerbose("  packageName:" + applicationContext.getPackageName(), applicationContext);

        context.put( "packageFolderPathName", applicationContext.getPackageFolderPathName());
        propertyRepository.put( "packageFolderPathName", applicationContext.getPackageFolderPathName());
        applicationUtils.printVerbose("  packageFolderPathName:" + applicationContext.getPackageFolderPathName(), applicationContext);

        context.put( "artifactId", applicationContext.getArtifactId());
        propertyRepository.put( "artifactId", applicationContext.getArtifactId());
        applicationUtils.printVerbose("  artifactId:" + applicationContext.getArtifactId(), applicationContext);
        context.put( "groupId", applicationContext.getGroupId());
        propertyRepository.put( "groupId", applicationContext.getGroupId());
        applicationUtils.printVerbose("  groupId:" + applicationContext.getGroupId(), applicationContext);
        context.put( "versionOfApplication", applicationContext.getVersionOfApplication());
        propertyRepository.put( "versionOfApplication", applicationContext.getVersionOfApplication());
        applicationUtils.printVerbose("  versionOfApplication:" + applicationContext.getVersionOfApplication(), applicationContext);

        String dependencies = extractApplicationTypeContent(applicationContext, actionRepository, PomFileDependencyActionClassName, applicationUtils);
        String properties = extractApplicationTypeContent(applicationContext, actionRepository, PomFilePropertyActionClassName, applicationUtils);
        if (Objects.nonNull(properties) && StringUtils.isNotBlank(properties)) {
            properties = "    <properties>\n" + properties + "    </properties>\n";
        }
        String plugins = extractApplicationTypeContent(applicationContext, actionRepository, PomFilePluginActionClassName, applicationUtils);


        context.put( "dependencies", dependencies);
        applicationUtils.printVerbose("  dependencies:" + dependencies, applicationContext);
        context.put( "properties", properties);
        applicationUtils.printVerbose("  properties:" + properties, applicationContext);

        StringWriter velocityEvaluatedPlugins = new StringWriter();
        Velocity.evaluate(context, velocityEvaluatedPlugins, "plugins", plugins);
        context.put( "plugins", velocityEvaluatedPlugins.toString());
        applicationUtils.printVerbose("  plugins:" + properties, applicationContext);

        return context;
    }

    void createFilesFromActionList(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils,
                                   ActionRepository actionRepository, PropertyRepository propertyRepository) {
        applicationUtils.printVerbose("Create files:", applicationContext);
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            createFileFromAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        }
    }

    void createFileFromAction(Action action, ApplicationContext applicationContext, Context velocityContext,
                              ApplicationUtils applicationUtils, PropertyRepository propertyRepository) {
        applicationUtils.printVerbose("createFileFromAction", applicationContext);
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                createFileFromAction(action1, applicationContext, velocityContext, applicationUtils, propertyRepository);
            }
        }
        else if (action instanceof FileAction) {
            applicationUtils.printVerbose("    Action is a FolderPathAction", applicationContext);
            FileAction fileAction = (FileAction) action;
            Path templatePath = null;
            if (Application.projectFolderPathNamePropertyName.equals(fileAction.getPropertyName())) {
                templatePath = Paths.get((String) propertyRepository.get(Application.projectFolderPathNamePropertyName));
            }
            else {
                templatePath = Paths.get((String) propertyRepository.get(Application.projectFolderPathNamePropertyName), (String) propertyRepository.get(fileAction.getPropertyName()));
            }
            applicationUtils.printVerbose("  template path: " + templatePath.toString() + ".", applicationContext);
            processVelocityTemplate(fileAction.getTargetFileName(), fileAction.getSourceFileName(), templatePath,
                    applicationContext, velocityContext, applicationUtils);
        }
    }

    void createFoldersFromActionList(ApplicationContext applicationContext, ApplicationUtils applicationUtils,
                                     ActionRepository actionRepository, PropertyRepository propertyRepository) {
        applicationUtils.printVerbose("Create folders:", applicationContext);
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            applicationUtils.printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        }
    }

    void createFoldersFromAction(Action action, ApplicationContext applicationContext,
                                 ApplicationUtils applicationUtils, PropertyRepository propertyRepository) {
        if (action instanceof ListOfAction) {
            applicationUtils.printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                createFoldersFromAction(action1, applicationContext, applicationUtils, propertyRepository);
            }
        }
        else if (action instanceof FolderPathAction) {
            applicationUtils.printVerbose("    Action is a FolderPathAction", applicationContext);
            FolderPathAction folderPathAction = (FolderPathAction) action;
            if (propertyRepository.containsNotProperty(folderPathAction.getPropertyName())) {
                throw new RuntimeException("Property name: '" + folderPathAction.getPropertyName() + "' is not stored in PropertyRepository.");
            }
            String folderPathName = (String) propertyRepository.get(folderPathAction.getPropertyName());
            if (StringUtils.isBlank(folderPathName)){
                throw new RuntimeException("Property name: '" + folderPathAction.getPropertyName() + "' is stored in PropertyRepository as empty string: '" + folderPathName + "'.");
            }
            else if (folderPathName.contains("${")) {
                throw new RuntimeException("Property name: '" + folderPathAction.getPropertyName() + "' is stored in PropertyRepository with variable name in it. See: '" + folderPathName + "'.");
            }
            Path folderPath = Path.of(propertyRepository.get(projectFolderPathPropertyName).toString(), folderPathName);
            try {
                FileUtils.forceMkdir(folderPath.toFile());
            } catch (IOException e) {
                applicationUtils.printError("Could not create folder '" + folderPath + "'.");
                System.exit(1);
            }
            applicationUtils.printVerbose("    Folder structure: '" + folderPath + "' created.", applicationContext);
            propertyRepository.put(folderPathAction.getPropertyName() + "Path", folderPath);
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

    void initializeVelocity(ApplicationContext applicationContext, ApplicationUtils applicationUtils) {
        applicationUtils.printVerbose("Initialize Velocity.", applicationContext);
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
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

    void createProjectFolder(ApplicationContext context, ApplicationUtils applicationUtils, PropertyRepository propertyRepository) {
        applicationUtils.printVerbose("Create Project Folder.", context);

        if (Objects.isNull(context.getProjectName()) || context.getProjectName().isBlank()) {
            throw new IllegalArgumentException("ProjectName in ApplicationContext is null.");
        }
        Path projectFolderPath = Path.of(context.getUserDir(), context.getProjectName());

        applicationUtils.printVerbose("Project Folder Path: '" + projectFolderPath + "'.", context);

        try {
            FileUtils.forceMkdir(projectFolderPath.toFile());
        } catch (IOException e) {
            applicationUtils.printError("Could not create project folder.");
            System.exit(1);
        }
        applicationUtils.printVerbose("Project Folder, '" + projectFolderPath.toString() + "', created.", context);

        propertyRepository.put(projectFolderPathPropertyName, projectFolderPath);
        propertyRepository.put(projectFolderPathNamePropertyName, projectFolderPath.toString());
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

    void createGitFiles(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils, PropertyRepository propertyRepository) {
        applicationUtils.printVerbose("Create git files.", applicationContext);

        if (applicationContext.isNoGit()) {
            applicationUtils.printVerbose("--no-git option true. No 'README.md' or '.gitignore' files will be created for the project.", applicationContext);
            return;
        }

        processVelocityTemplate("README.md", "README.vtl", (Path) propertyRepository.get(projectFolderPathPropertyName),
                applicationContext, velocityContext, applicationUtils);

        processVelocityTemplate(".gitignore", "gitignore.vtl", (Path) propertyRepository.get(projectFolderPathPropertyName),
                applicationContext, velocityContext, applicationUtils);
    }
}
