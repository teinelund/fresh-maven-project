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
import org.teinelund.freshmavenproject.action.ListOfAction;
import org.teinelund.freshmavenproject.action.PomFileDependencyAction;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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

    public static void main(String[] args) {
        Application application = new Application();
        CommandLineOptions options = new CommandLineOptions();
        ApplicationUtils applicationUtils = new ApplicationUtils();

        ApplicationTypes applicationTypes = new ApplicationTypes();
        ActionRepository actionRepository = new ActionRepository();
        ApplicationContext applicationContext = new ApplicationContext();

        try {
            application.execute(args, options, applicationTypes, actionRepository, applicationContext, applicationUtils);
        }
        catch(Exception e) {
            printError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void execute(String[] args, CommandLineOptions options,
                        ApplicationTypes applicationTypes, ActionRepository actionRepository,
                        ApplicationContext applicationContext, ApplicationUtils applicationUtils) {

        parseCommandLineOptions(args, options);

        ifHelpOptionOrVersionOption(options, applicationUtils);

        printVerbose("Verbose mode on.", options);

        verifyParameters(options, applicationUtils);

        interactiveMode(options, applicationTypes, applicationContext);
        nonInteractiveMode(options, applicationTypes, applicationContext);

        createProjectFolder(applicationContext);

        Context velocityContext = initializeVelocity(applicationContext, actionRepository);

        createPomFile(applicationContext, velocityContext, applicationUtils);

        createMavenSourceAndTestPathsAndPackagePaths(applicationContext);

        createSrcFolderWithSubFolders(applicationContext);

        createApplicationSourceFile(applicationContext, velocityContext, applicationUtils);

        createApplicationTestSourceFile(applicationContext, velocityContext, applicationUtils);

        createGitFiles(applicationContext, velocityContext, applicationUtils);
    }

    void createMavenSourceAndTestPathsAndPackagePaths(ApplicationContext context) {
        String[] folderNames = context.getPackageName().split("\\.");
        printVerbose("Folder names: " + Arrays.toString(folderNames) + ".", context);
        Path srcMainJava = Path.of(context.getProjectFolder().toString(), "src", "main", "java");
        context.setSrcMainJavaPath(srcMainJava);
        context.setSrcMainJavaPackagePath(Path.of(srcMainJava.toString(), folderNames));
        Path srcTestJava = Path.of(context.getProjectFolder().toString(), "src", "test", "java");
        context.setSrcTestJavaPath(srcTestJava);
        context.setSrcTestJavaPackagePath(Path.of(srcTestJava.toString(), folderNames));
    }

    void interactiveMode(CommandLineOptions options, ApplicationTypes applicationTypes, ApplicationContext context) {
        if (options.isInteractive()) {

            context.setVerbose(options.isVerbose());

            printInteractive("Interactive mode.");
            printInteractive("In this mode you type all the information by hand. First set values to groupId, artifactId, version,");
            printInteractive("and project name. After that choose what type of application you want to build: stand-alone-application");
            printInteractive("library JAR or a J2EE application. To the last part chose what technologies you want to use in your application.");
            printInteractive("In all questions bellow: q=quit.");
            printInteractive("Illegal characters will restart the query until correct. Default values in parenthesis, may be chosen");
            printInteractive("by pressing Enter key.");
            printInteractive("");

            context.setGroupId(interactiveQuery("groupId"));
            context.setArtifactId(interactiveQuery("artifactId"));
            context.setVersionOfApplication(interactiveQuery("version", "1.0.0-SNAPSHOT"));

            printInteractive("Project name is used as project folder name, 'name' tag in pom file and 'finalName' tag in pom file. ArtifactId is default value.");
            context.setProjectName(interactiveQuery("project name", context.getArtifactId()));

            context.setProgrameNameUsedInPrintVersion(createProgramNameUsedInPrintVersion(context.getProjectName()));

            String packageName = replaceMinusAndUnderscore(context.getGroupId() + "." + context.getArtifactId());
            String folderPath = packageName.replaceAll("\\.", "/");
            printInteractive("Root package is (groupId + artifactId): " + packageName + " . This will also produce the folder path");
            printInteractive("\"" + folderPath + "\" in src/main and src/test.");
            String newPackageName = interactiveQuery("package name", packageName);
            context.setPackageName(newPackageName);
            context.setFolderPath(folderPath);
            if (! newPackageName.equals(packageName)) {
                String newFolderPath = newPackageName.replaceAll("\\.", "/");
                printInteractive("New folder path will be \"" + newFolderPath + "\" in src/main and src/test.");
                context.setFolderPath(newFolderPath);
            }

            printVerbose("Project name: '" + context.getProjectName() + "', packageName: '" +
                    context.getPackageName() + "'.", context);

            printInteractive("");
            printInteractive("What kind of application do you want to create?");
            int defaultOptionIndex = 1;
            TypeOfApplication typeOfApplication = interactiveQueryTypeOfApplication("Type of application", TypeOfApplication.values(), defaultOptionIndex);
            context.setTypeOfApplication(typeOfApplication);
            printInteractive("You selected to create a " + typeOfApplication.getDescription() + ".");
            printInteractive("");

            printInteractive("What kind of stack of technologies/dependencies do you want to use?");
            List<ApplicationType> queryOptions = applicationTypes.getQueries(typeOfApplication);
            ApplicationType applicationType = interactiveQuery("Stack of technologies/dependencies", queryOptions, 1);
            context.setApplicationType(applicationType);
            printInteractive("");

            printInteractive("Should GIT files (README.md, .gitignore) be created?");
            Collection<String> queryOptionsYN = List.of("y", "n");
            context.setNoGit(interactiveQuery("Git files (y/n)", queryOptionsYN, "y"));
        }
    }

    void nonInteractiveMode(CommandLineOptions options, ApplicationTypes applicationTypes, ApplicationContext applicationContext) {
        if (!options.isInteractive()) {
            printInteractive("Non interactive mode not supported, yet.");
            System.exit(1);
        }
    }

    String replaceMinusAndUnderscore(String text) {
        return text.replaceAll("-", "").replaceAll("_", "");
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

    String interactiveQuery(String question) {
        return interactiveQuery(question, "");
    }

    String interactiveQuery(String question, String defaultValue) {
        String query;
        do {
            printInteractiveQuestion(question + (Objects.isNull(defaultValue) || defaultValue.isBlank() ? ": " : " (" + defaultValue + "): "));
            Scanner in = new Scanner(System.in);
            query = in.nextLine();
        } while((Objects.isNull(defaultValue) || defaultValue.isBlank()) && query.isBlank());
        if ("q".equals(query)) {
            System.exit(0);
        }
        if (Objects.nonNull(defaultValue) && !defaultValue.isBlank() && query.isBlank()) {
            query = defaultValue;
        }
        return query;
    }

    String interactiveQuery(String question, Collection<String> options, String defaultValue) {
        String query = "";
        do {
            StringBuilder queryText = new StringBuilder();
            queryText.append(question);
            queryText.append(" [");
            int index = 0;
            for (String option : options) {
                if (index > 0) {
                    queryText.append(", ");
                }
                queryText.append(option);
                index++;
            }
            queryText.append("] ");
            queryText.append(Objects.isNull(defaultValue) || defaultValue.isBlank() ? ": " : " (" + defaultValue + "): ");
            printInteractiveQuestion(queryText.toString());
            Scanner in = new Scanner(System.in);
            query = in.nextLine();
        } while(((Objects.isNull(defaultValue) || defaultValue.isBlank()) && query.isBlank()) ||
                (!query.isBlank() && ! options.contains(query)));
        if ("q".equals(query)) {
            System.exit(0);
        }
        if (Objects.nonNull(defaultValue) && !defaultValue.isBlank() && query.isBlank()) {
            query = defaultValue;
        }
        return query;
    }

    TypeOfApplication interactiveQueryTypeOfApplication(String question, TypeOfApplication[] queryOptions, int defaultOptionIndex) {
        printInteractive("Select one of the following options:");
        if (defaultOptionIndex < 0 || defaultOptionIndex >= queryOptions.length) {
            throw new IllegalArgumentException("Default option index " + Integer.toString(defaultOptionIndex) +
                    " has a illegal value. Legal value are 0 to " + Integer.toString(queryOptions.length - 1) + ".");
        }
        int index = 1;
        for (TypeOfApplication queryOption : queryOptions) {
            printInteractive("  " + Integer.toString(index) + ". " + queryOption.getDescription());
            index++;
        }
        int answerIndex = -1;
        String answer = "";
        while(true) {
            answer = interactiveQuery("Type of application (1-" + Integer.toString(queryOptions.length) + ")?",
                    Integer.toString(defaultOptionIndex));
            try {
                answerIndex = Integer.parseInt(answer);
            }
            catch (Exception ex) {
                continue;
            }
            if (answerIndex < 1 || answerIndex > queryOptions.length) {
                continue;
            }
            break;
        }

        return queryOptions[answerIndex - 1];
    }

    ApplicationType interactiveQuery(String question, List<ApplicationType> queryOptions, int defaultOptionIndex) {
        printInteractive("Select one of the following options:");
        if (defaultOptionIndex < 1 || defaultOptionIndex > queryOptions.size()) {
            throw new IllegalArgumentException("Default option index " + Integer.toString(defaultOptionIndex) + " has a illegal value. Legal value are 1 to " + Integer.toString(queryOptions.size()) + ".");
        }
        int index = 1;
        for (ApplicationType queryOption : queryOptions) {
            printInteractive("  " + Integer.toString(index) + ". " + queryOption.getDescription());
            index++;
        }
        int answerIndex = -1;
        String answer = "";
        while(true) {
            answer = interactiveQuery("Type of application (1-" + Integer.toString(queryOptions.size()) + ")?", Integer.toString(defaultOptionIndex));
            try {
                answerIndex = Integer.parseInt(answer);
            }
            catch (Exception ex) {
                continue;
            }
            if (answerIndex < 1 || answerIndex > queryOptions.size()) {
                continue;
            }
            break;
        }

        return queryOptions.get(answerIndex - 1);
    }

    VelocityContext initializeVelocity(ApplicationContext applicationContext, ActionRepository actionRepository) {
        printVerbose("Method initializeVelocity:", applicationContext);
        String dependencies = extractApplicationTypeContent(applicationContext, actionRepository, PomFileDependencyActionClassName);
        String properties = extractApplicationTypeContent(applicationContext, actionRepository, PomFilePropertyActionClassName);
        if (Objects.nonNull(properties) && StringUtils.isNotBlank(properties)) {
            properties = "    <properties>\n" + properties + "    </properties>\n";
        }
        printVerbose("  projectName:" + applicationContext.getProjectName(), applicationContext);
        printVerbose("  programNameUsedInPrintVersion:" + applicationContext.getProgramNameUsedInPrintVersion(), applicationContext);
        printVerbose("  packageName:" + applicationContext.getPackageName(), applicationContext);
        printVerbose("  artifactId:" + applicationContext.getArtifactId(), applicationContext);
        printVerbose("  groupId:" + applicationContext.getGroupId(), applicationContext);
        printVerbose("  versionOfApplication:" + applicationContext.getVersionOfApplication(), applicationContext);
        printVerbose("  dependencies:" + dependencies, applicationContext);
        printVerbose("  properties:" + properties, applicationContext);
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
        context.put( "dependencies", dependencies);
        context.put( "properties", properties);
        return context;
    }

    String extractApplicationTypeContent(ApplicationContext applicationContext, ActionRepository actionRepository, String actionClassName) {
        printVerbose("Method extractApplicationTypeContent", applicationContext);
        StringBuilder dependencies = new StringBuilder();
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            printVerbose("  Action name: " + actionName, applicationContext);
            Action action = actionRepository.getAction(actionName);
            dependencies.append(extractSpecificActionContent(action, applicationContext, actionClassName));
        }
        return dependencies.toString();
    }

    String extractSpecificActionContent(Action action, ApplicationContext applicationContext, String actionClassName) {
        StringBuilder dependencies = new StringBuilder();
        String className = action.getClass().getName();
        if (action instanceof ListOfAction) {
            printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                dependencies.append(extractSpecificActionContent(action1, applicationContext, actionClassName));
            }
        }
        else if (className.equals(actionClassName)) {
            printVerbose("    Action is a " + actionClassName, applicationContext);
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

    String createProgramNameUsedInPrintVersion(String name) {
        String versionName = name.replaceAll("-", " ").replaceAll("_", " ");
        String versionNameArr[] = versionName.split(" ");
        for (int i=0; i<versionNameArr.length; i++) {
            versionNameArr[i] = StringUtils.capitalize(versionNameArr[i]);
        }
        versionName = String.join(" ", versionNameArr);
        return versionName;
    }

    void verifyParameters(CommandLineOptions options, ApplicationUtils applicationUtils) {
        printVerbose("Verify Command Line Parameters.", options);

        if (!options.isInteractive()) {
            if (Objects.isNull(options.getGroupid()) || options.getGroupid().isBlank()) {
                printError("Group id is mandatory.");
                applicationUtils.exitError();
            }
            if (Objects.isNull(options.getArtifactId()) || options.getArtifactId().isBlank()) {
                printError("Artifact id is mandatory.");
                applicationUtils.exitError();
            }
        }
    }

    void createProjectFolder(ApplicationContext context) {
        printVerbose("Create Project Folder.", context);
        String projectFolderName = context.getArtifactId();
        if (StringUtils.isNotBlank(context.getProjectName())) {
            projectFolderName = context.getProjectName();
        }
        Path projectFolder = Path.of(context.getUserDir(), projectFolderName);

        printVerbose("Project Folder Path: '" + projectFolder.toString() + "'.", context);

        try {
            FileUtils.forceMkdir(projectFolder.toFile());
        } catch (IOException e) {
            printError("Could not create project folder.");
            System.exit(1);
        }
        printVerbose("Project Folder, '" + projectFolder.toString() + "', created.", context);

        context.setProjectFolder(projectFolder);
    }

    void createPomFile(ApplicationContext applicationContext, Context context, ApplicationUtils applicationUtils) {

        processVelocityTemplate("pom.xml", "pom.vtl", applicationContext.getProjectFolder(),
                applicationContext, context, applicationUtils);
    }

    void createSrcFolderWithSubFolders(ApplicationContext context) {
        printVerbose("Create 'src' folder with sub folders.", context);
        printVerbose("Java source path: '" + context.getSrcMainJavaPackagePath().toString() + "', java test path: '" +
                context.getSrcTestJavaPackagePath().toString() + "'.", context);

        try {
            FileUtils.forceMkdir(context.getSrcMainJavaPackagePath().toFile());
        } catch (IOException e) {
            printError("Could not create folder '" + context.getSrcMainJavaPackagePath().toString() + "'.");
            System.exit(1);
        }
        printVerbose("Folder structure: '" + context.getSrcMainJavaPackagePath().toString() + "' created.", context);

        try {
            FileUtils.forceMkdir(context.getSrcTestJavaPackagePath().toFile());
        } catch (IOException e) {
            printError("Could not create folder '" + context.getSrcTestJavaPackagePath().toString() + "'.");
            System.exit(1);
        }
        printVerbose("Folder structure: '" + context.getSrcTestJavaPackagePath().toString() + "' created.", context);
    }

    void processVelocityTemplate(String targetFileName, String templateName, Path targetPath, ApplicationContext applicationContext,
                                 Context context, ApplicationUtils applicationUtils) {
        printVerbose("Create '" + targetFileName + "' source file.", applicationContext);

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
            printError("Could not create '" + targetFileName + "' file.");
            applicationUtils.exitError();
        }
    }

    void createApplicationSourceFile(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        processVelocityTemplate("Application.java", "Application.vtl",
                applicationContext.getSrcMainJavaPackagePath(), applicationContext, velocityContext, applicationUtils);
    }

    void createApplicationTestSourceFile(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        processVelocityTemplate("ApplicationTest.java", "ApplicationTest.vtl",
                applicationContext.getSrcTestJavaPackagePath(), applicationContext, velocityContext, applicationUtils);
    }

    void createGitFiles(ApplicationContext applicationContext, Context velocityContext, ApplicationUtils applicationUtils) {
        printVerbose("Create git files.", applicationContext);

        if (applicationContext.isNoGit()) {
            printVerbose("--no-git option true. No 'README.md' or '.gitignore' files will be created for the project.", applicationContext);
            return;
        }

        processVelocityTemplate("README.md", "README.vtl", applicationContext.getProjectFolder(),
                applicationContext, velocityContext, applicationUtils);

        processVelocityTemplate(".gitignore", "gitignore.vtl", applicationContext.getProjectFolder(),
                applicationContext, velocityContext, applicationUtils);
    }

    static void printInteractive(String message) {
        System.out.println(message);
    }

    static void printInteractiveQuestion(String message) {
        System.out.print(message);
    }

    static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    void printVerbose(String message, Verbosable verbosable) {
        if (verbosable.isVerbose()) {
            System.out.println("[VERBOSE] " + message);
        }
    }
}
