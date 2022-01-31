package org.teinelund.freshmavenproject;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class InteractiveQueryEngine {

    public void userQueries(CommandLineOptions options, ApplicationTypes applicationTypes, ApplicationContext context, ApplicationUtils applicationUtils) {
        context.setVerbose(options.isVerbose());

        applicationUtils.printInteractive("Interactive mode.");
        applicationUtils.printInteractive("In this mode you type all the information by hand. First set values to groupId, artifactId, version,");
        applicationUtils.printInteractive("and project name. After that choose what type of application you want to build: stand-alone-application");
        applicationUtils.printInteractive("library JAR or a J2EE application. To the last part chose what technologies you want to use in your application.");
        applicationUtils.printInteractive("In all questions bellow: q=quit.");
        applicationUtils.printInteractive("Illegal characters will restart the query until correct. Default values in parenthesis, may be chosen");
        applicationUtils.printInteractive("by pressing Enter key.");
        applicationUtils.printInteractive("");

        context.setGroupId(interactiveQuery("groupId", applicationUtils));
        context.setArtifactId(interactiveQuery("artifactId", applicationUtils));
        context.setVersionOfApplication(interactiveQuery("version", "1.0.0-SNAPSHOT", applicationUtils));

        applicationUtils.printInteractive("Project name is used as project folder name, 'name' tag in pom file and 'finalName' tag in pom file. ArtifactId is default value.");
        context.setProjectName(interactiveQuery("project name", context.getArtifactId(), applicationUtils));

        context.setProgrameNameUsedInPrintVersion(createProgramNameUsedInPrintVersion(context.getProjectName()));

        String packageName = replaceMinusAndUnderscore(context.getGroupId() + "." + context.getArtifactId());
        String packageFolderPathName = packageName.replaceAll("\\.", "/");
        applicationUtils.printInteractive("Root package is (groupId + artifactId): " + packageName + " . This will also produce the folder path");
        applicationUtils.printInteractive("\"" + packageFolderPathName + "\" in src/main/java and src/test/java.");
        String newPackageName = interactiveQuery("package name", packageName, applicationUtils);
        context.setPackageName(newPackageName);
        context.setPackageFolderPathName(packageFolderPathName);
        if (! newPackageName.equals(packageName)) {
            String newPackageFolderPathName = newPackageName.replaceAll("\\.", "/");
            applicationUtils.printInteractive("New folder path will be \"" + newPackageFolderPathName + "\" in src/main/java and src/test/java.");
            context.setPackageFolderPathName(newPackageFolderPathName);
        }

        applicationUtils.printVerbose("Project name: '" + context.getProjectName() + "', packageName: '" +
                context.getPackageName() + "'.", context);

        applicationUtils.printInteractive("");
        applicationUtils.printInteractive("What kind of application do you want to create?");
        int defaultOptionIndex = 1;
        TypeOfApplication typeOfApplication =
                interactiveQueryTypeOfApplication("Type of application", TypeOfApplication.values(),
                        defaultOptionIndex, applicationUtils);
        context.setTypeOfApplication(typeOfApplication);
        applicationUtils.printInteractive("You selected to create a " + typeOfApplication.getDescription() + ".");
        applicationUtils.printInteractive("");

        applicationUtils.printInteractive("What kind of stack of technologies/dependencies do you want to use?");
        List<ApplicationType> queryOptions = applicationTypes.getQueries(typeOfApplication);
        ApplicationType applicationType = interactiveQuery("Stack of technologies/dependencies", queryOptions, 1, applicationUtils);
        context.setApplicationType(applicationType);
        applicationUtils.printInteractive("");

        applicationUtils.printInteractive("Should GIT files (README.md, .gitignore) be created?");
        Collection<String> queryOptionsYN = List.of("y", "n");
        context.setNoGit(interactiveQuery("Git files (y/n)", queryOptionsYN, "y", applicationUtils));
    }

    String interactiveQuery(String question, ApplicationUtils applicationUtils) {
        return interactiveQuery(question, "", applicationUtils);
    }

    String interactiveQuery(String question, String defaultValue, ApplicationUtils applicationUtils) {
        String query;
        do {
            applicationUtils.printInteractiveQuestion(question + (Objects.isNull(defaultValue) || defaultValue.isBlank() ? ": " : " (" + defaultValue + "): "));
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

    String createProgramNameUsedInPrintVersion(String name) {
        String versionName = name.replaceAll("-", " ").replaceAll("_", " ");
        String versionNameArr[] = versionName.split(" ");
        for (int i=0; i<versionNameArr.length; i++) {
            versionNameArr[i] = StringUtils.capitalize(versionNameArr[i]);
        }
        versionName = String.join(" ", versionNameArr);
        return versionName;
    }

    String replaceMinusAndUnderscore(String text) {
        return text.replaceAll("-", "").replaceAll("_", "");
    }

    TypeOfApplication interactiveQueryTypeOfApplication(String question, TypeOfApplication[] queryOptions, int defaultOptionIndex,
                                                                    ApplicationUtils applicationUtils) {
        applicationUtils.printInteractive("Select one of the following options:");
        if (defaultOptionIndex < 0 || defaultOptionIndex >= queryOptions.length) {
            throw new IllegalArgumentException("Default option index " + Integer.toString(defaultOptionIndex) +
                    " has a illegal value. Legal value are 0 to " + Integer.toString(queryOptions.length - 1) + ".");
        }
        int index = 1;
        for (TypeOfApplication queryOption : queryOptions) {
            applicationUtils.printInteractive("  " + Integer.toString(index) + ". " + queryOption.getDescription());
            index++;
        }
        int answerIndex = -1;
        String answer = "";
        while(true) {
            answer = interactiveQuery("Type of application (1-" + Integer.toString(queryOptions.length) + ")?",
                    Integer.toString(defaultOptionIndex), applicationUtils);
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

    ApplicationType interactiveQuery(String question, List<ApplicationType> queryOptions, int defaultOptionIndex, ApplicationUtils applicationUtils) {
        applicationUtils.printInteractive("Select one of the following options:");
        if (defaultOptionIndex < 1 || defaultOptionIndex > queryOptions.size()) {
            throw new IllegalArgumentException("Default option index " + Integer.toString(defaultOptionIndex) + " has a illegal value. Legal value are 1 to " + Integer.toString(queryOptions.size()) + ".");
        }
        int index = 1;
        for (ApplicationType queryOption : queryOptions) {
            applicationUtils.printInteractive("  " + Integer.toString(index) + ". " + queryOption.getDescription());
            index++;
        }
        int answerIndex = -1;
        String answer = "";
        while(true) {
            answer = interactiveQuery("Type of application (1-" + Integer.toString(queryOptions.size()) + ")?", Integer.toString(defaultOptionIndex),
                    applicationUtils);
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

    String interactiveQuery(String question, Collection<String> options, String defaultValue, ApplicationUtils applicationUtils) {
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
            applicationUtils.printInteractiveQuestion(queryText.toString());
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
}
