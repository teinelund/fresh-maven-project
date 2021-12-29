package org.teinelund.freshmavenproject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
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

    private ActionRepository actionRepository = new ActionRepository();

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

        ApplicationTypes applicationTypes = new ApplicationTypes();

        ApplicationContext applicationContext = new ApplicationContext();
        interactiveMode(options, applicationTypes, applicationContext);
        nonInteractiveMode(options, applicationTypes, applicationContext);

        createProjectFolder(applicationContext);

        Context velocityContext = initializeVelocity(applicationContext);

        printVerbose("Project name: '" + applicationContext.getProjectName() + "', packageName: '" + applicationContext.getPackageName() +
                "'.", applicationContext);

        createPomFile(applicationContext, velocityContext);

        String[] folderNames = applicationContext.getPackageName().split("\\.");
        printVerbose("Folder names: " + Arrays.toString(folderNames) + ".", applicationContext);
        Path srcMainJava = Path.of(applicationContext.getProjectFolder().toString(), "src", "main", "java");
        Path srcMainJavaPackagePath = Path.of(srcMainJava.toString(), folderNames);
        Path srcTestJava = Path.of(applicationContext.getProjectFolder().toString(), "src", "test", "java");
        Path srcTestJavaPackagePath = Path.of(srcTestJava.toString(), folderNames);

        createSrcFolderWithSubFolders(applicationContext.getProjectFolder(), srcMainJavaPackagePath, srcTestJavaPackagePath, applicationContext);

        createApplicationSourceFile(srcMainJavaPackagePath, applicationContext, velocityContext);

        createApplicationTestSourceFile(srcTestJavaPackagePath, applicationContext, velocityContext);

        createGitFiles(applicationContext.getProjectFolder(), applicationContext.getProgramNameUsedInPrintVersion(), applicationContext, velocityContext);
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

    VelocityContext initializeVelocity(ApplicationContext applicationContext) {
        printVerbose("Method initializeVelocity:", applicationContext);
        String dependencies = extractDependencies(applicationContext);
        printVerbose("  projectName:" + applicationContext.getProjectName(), applicationContext);
        printVerbose("  programNameUsedInPrintVersion:" + applicationContext.getProgramNameUsedInPrintVersion(), applicationContext);
        printVerbose("  packageName:" + applicationContext.getPackageName(), applicationContext);
        printVerbose("  artifactId:" + applicationContext.getArtifactId(), applicationContext);
        printVerbose("  groupId:" + applicationContext.getGroupId(), applicationContext);
        printVerbose("  versionOfApplication:" + applicationContext.getVersionOfApplication(), applicationContext);
        printVerbose("  dependencies:" + dependencies, applicationContext);
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
        return context;
    }

    String extractDependencies(ApplicationContext applicationContext) {
        printVerbose("Method extractDependencies", applicationContext);
        StringBuilder dependencies = new StringBuilder();
        for (String actionName : applicationContext.getApplicationType().getActionNames()) {
            printVerbose("  Action name: " + actionName, applicationContext);
            Action action = this.actionRepository.getAction(actionName);
            dependencies.append(extractPomFileDependencyAction(action, applicationContext));
        }
        return dependencies.toString();
    }

    String extractPomFileDependencyAction(Action action, ApplicationContext applicationContext) {
        StringBuilder dependencies = new StringBuilder();
        if (action instanceof ListOfAction) {
            printVerbose("    Action is a ListOfAction", applicationContext);
            ListOfAction listOfAction = (ListOfAction) action;
            for (Action action1 : listOfAction) {
                dependencies.append(extractPomFileDependencyAction(action1, applicationContext));
            }
        }
        else if (action instanceof PomFileDependencyAction) {
            printVerbose("    Action is a PomFileDependencyAction", applicationContext);
            PomFileDependencyAction dependencyAction = (PomFileDependencyAction) action;
            dependencies.append(dependencyAction.getDependencyContent());
        }
        return dependencies.toString();
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

    String createProgramNameUsedInPrintVersion(String name) {
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

        if (!options.isInteractive()) {
            if (Objects.isNull(options.getGroupid()) || options.getGroupid().isBlank()) {
                printError("Group id is mandatory.");
                System.exit(1);
            }
            if (Objects.isNull(options.getArtifactId()) || options.getArtifactId().isBlank()) {
                printError("Artifact id is mandatory.");
                System.exit(1);
            }
        }
    }

    void createProjectFolder(ApplicationContext context) {
        printVerbose("Create Project Folder.", context);
        String projectFolderName = context.getArtifactId();
        if (!Objects.isNull(context.getProjectName()) && !context.getProjectName().isBlank()) {
            projectFolderName = context.getProjectName();
        }
        Path projectFolder = Path.of(SystemUtils.USER_DIR, projectFolderName);

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

    void createPomFile(ApplicationContext applicationContext, Context context) {

        processVelocityTemplate("pom.xml", "pom.vtl", applicationContext.getProjectFolder(),
                applicationContext, context);
    }

    void createSrcFolderWithSubFolders(Path projectFolder, Path srcMainJavaPackagePath, Path srcTestJavaPackagePath,
                                       ApplicationContext options) {
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

    void processVelocityTemplate(String targetFileName, String templateName, Path targetPath, ApplicationContext applicationContext, Context context) {
        printVerbose("Create '" + targetFileName + "' source file.", applicationContext);

        Path targetFilePath = Path.of(targetPath.toString(), targetFileName);

        Template template = null;

        try
        {
            template = Velocity.getTemplate("templates/" + templateName);
        } catch( Exception e ) {
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
                                     ApplicationContext options, Context context) {
        processVelocityTemplate("Application.java", "Application.vtl", srcMainJavaPackagePath,
                options, context);
    }

    void createApplicationTestSourceFile(Path srcTestJavaPackagePath, ApplicationContext options, Context context) {

        processVelocityTemplate("ApplicationTest.java", "ApplicationTest.vtl", srcTestJavaPackagePath,
                options, context);
    }

    void createGitFiles(Path projectFolder, String versionName, ApplicationContext applicationContext, Context context) {
        printVerbose("Create git files.", applicationContext);

        if (applicationContext.isNoGit()) {
            printVerbose("--no-git option true. No 'README.md' or '.gitignore' files will be created for the project.", applicationContext);
            return;
        }

        processVelocityTemplate("README.md", "README.vtl", projectFolder,
                applicationContext, context);

        processVelocityTemplate(".gitignore", "gitignore.vtl", projectFolder,
                applicationContext, context);
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
