package org.teinelund.freshmavenproject;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.teinelund.freshmavenproject.action.Action;
import org.teinelund.freshmavenproject.action.ActionRepository;
import org.teinelund.freshmavenproject.action.FolderPathAction;
import org.teinelund.freshmavenproject.action.ListOfAction;
import org.teinelund.freshmavenproject.action.PomFileDependencyAction;
import org.teinelund.freshmavenproject.action.PomFilePropertyAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.teinelund.freshmavenproject.Application.PomFileDependencyActionClassName;
import static org.teinelund.freshmavenproject.Application.PomFilePluginActionClassName;
import static org.teinelund.freshmavenproject.Application.PomFilePropertyActionClassName;
import static org.teinelund.freshmavenproject.Application.projectFolderPathPropertyName;

@ExtendWith(MockitoExtension.class)
public class ApplicationTest {

    private Application sut = null;
    private static final String artifactId = "PROJECT_1";
    static final String dependencyContent = "CONTENT";
    private static final String dependencyContent2 = "CONTENT2";
    private static final String folderPath = "FOLDER_PATH";
    private static final String propertyName = "PROPERTY_NAME";
    private static final String action1 = "ACTION_1";
    private static final String action2 = "ACTION_2";
    private static Context context;
    private static final String programNameUsedInPrintVersion = "PROGRAM_NAME";
    private static final String PROJECT_NAME = "PROJECT_NAME";
    private static final String PROGRAM_NAME_USED_IN_PRINT_VERSION = "PROGRAM_NAME_USED_IN_PRINT_VERSION";
    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String ARTIFACT_ID = "ARTIFACT_ID";
    private static final String GROUPR_ID = "GROUPR_ID";
    private static final String VERSION_OF_APPLICATION = "VERSION_OF_APPLICATION";
    private static final String DEPENDECIES_STRING = "DEPENDECIES_STRING";
    private static final String PROPERTIES_STRING = "PROPERTIES_STRING";
    private static final String PLUGINS_STRING = "PLUGINS_STRING";
    private static final String PROJECT_NAME_STRING_KEY = "projectName";
    private static final String PROGRAM_NAME_USED_IN_PRINT_VERSION_STRING_KEY = "programNameUsedInPrintVersion";
    private static final String PACKAGE_NAME_STRING_KEY = "packageName";
    private static final String ARTIFACT_ID_STRING_KEY = "artifactId";
    private static final String GROUPR_ID_STRING_KEY = "groupId";
    private static final String VERSION_OF_APPLICATION_STRING_KEY = "versionOfApplication";
    private static final String DEPENDECIES_STRING_STRING_KEY = "dependencies";
    private static final String PROPERTIES_STRING_STRING_KEY = "properties";
    private static final String PLUGINS_STRING_STRING_KEY = "plugins";
    private static final String CONTEXT_FOLDER_PATH_KEY_1 = "pathVar1";
    private static final String CONTEXT_FOLDER_PATH_KEY_2 = "pathVar2";
    private static final String CONTEXT_FOLDER_PATH_VALUE = "/path1/path2";
    private static final String FOLDER_PATH_ACTION_CONTENT_1 = "${" + CONTEXT_FOLDER_PATH_KEY_1 + "}/path3";
    private static final String FOLDER_PATH_ACTION_CONTENT_2 = "${" + CONTEXT_FOLDER_PATH_KEY_1 + "}/${" + CONTEXT_FOLDER_PATH_KEY_2 +  "}/path3";
    private static final String FOLDER_PATH_ACTION_CONTENT_RESULT = CONTEXT_FOLDER_PATH_VALUE + "/path3";
    private static final String FOLDER_PATH_KEY = "pathKey";


    @Mock
    private CommandLineOptions options;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ApplicationUtils applicationUtils;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Spy
    private Application sutSpy = new Application();

    @BeforeAll
    static void initUnitTestSuite() {
        context = initializeVelocityForUnitTests();
    }

    @BeforeEach
    void init(TestInfo testInfo) {
        this.sut = new Application();
    }

    //
    // parseCommandLineOptions
    //

    @Test
    void parseCommandLineOptions() {
        // Initialize
        String[] args = {"-h"};
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        verify(options, times(1)).parse(any());
    }

    //
    // ifHelpOptionOrVersionOption
    //

    @Test
    void ifHelpOptionOrVersionOptionWhereHelpAndVersionIsFalse() {
        // Initialize
        when(options.isHelp()).thenReturn(false);
        when(options.isVersion()).thenReturn(false);
        // Test
        this.sut.ifHelpOptionOrVersionOption(options, applicationUtils);
        // Verify
        verify(options, never()).printHelp();
        verify(options, never()).printVersion();
        verify(applicationUtils, never()).exit();
    }

    @Test
    void ifHelpOptionOrVersionOptionWhereHelpIsTrue() {
        // Initialize
        when(options.isHelp()).thenReturn(true);

        // Unnecessary stubbings detected.
        // Clean & maintainable test code requires zero unnecessary code.
        // Following stubbings are unnecessary (click to navigate to relevant line of code):
        //  1. -> at org.teinelund.freshmavenproject.ApplicationTest.ifHelpOptionOrVersionOptionWhereHelpIsTrue(ApplicationTest.java:240)
        // Please remove unnecessary stubbings or use 'lenient' strictness. More info: javadoc for UnnecessaryStubbingException class.
        //
        // when(options.isVersion()).thenReturn(false);

        // Test
        this.sut.ifHelpOptionOrVersionOption(options, applicationUtils);
        // Verify
        verify(options, times(1)).printHelp();
        verify(options, never()).printVersion();
        verify(applicationUtils, times(1)).exit();
    }

    @Test
    void ifHelpOptionOrVersionOptionWhereVersionIsTrue() {
        // Initialize
        when(options.isHelp()).thenReturn(false);
        when(options.isVersion()).thenReturn(true);
        // Test
        this.sut.ifHelpOptionOrVersionOption(options, applicationUtils);
        // Verify
        verify(options, never()).printHelp();
        verify(options, times(1)).printVersion();
        verify(applicationUtils, times(1)).exit();
    }

    //
    // verifyParameters
    //

    @Test
    void verifyParametersWhereOptionsIsInteractive() {
        // Initialize
        when(options.isInteractive()).thenReturn(true);
        // Test
        this.sut.verifyParameters(options, applicationUtils);
        // Verify
        verify(options, never()).getGroupid();
        verify(options, never()).getArtifactId();
        verify(applicationUtils, never()).exitError();
    }

    //
    // interactiveMode
    //

    //
    // nonInteractiveMode
    //

    //
    // initializeVelocity
    //

    @Test
    void initializeVelocityWhereExceptionsAreNotExpected() {
        // Initialize
        // Test
        this.sut.initializeVelocity(applicationContext, applicationUtils);
        // Verify
    }

    //
    // initializeVelocityContext
    //
    @Test
    void initializeVelocityContext() {
        // Initialize
        when(applicationContext.getProjectName()).thenReturn(PROJECT_NAME);
        when(applicationContext.getProgramNameUsedInPrintVersion()).thenReturn(PROGRAM_NAME_USED_IN_PRINT_VERSION);
        when(applicationContext.getPackageName()).thenReturn(PACKAGE_NAME);
        when(applicationContext.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(applicationContext.getGroupId()).thenReturn(GROUPR_ID);
        when(applicationContext.getVersionOfApplication()).thenReturn(VERSION_OF_APPLICATION);
        doReturn(DEPENDECIES_STRING).when(this.sutSpy).extractApplicationTypeContent(applicationContext, actionRepository,
                PomFileDependencyActionClassName, applicationUtils);
        doReturn(PROPERTIES_STRING).when(this.sutSpy).extractApplicationTypeContent(applicationContext, actionRepository,
                PomFilePropertyActionClassName, applicationUtils);
        doReturn(PLUGINS_STRING).when(this.sutSpy).extractApplicationTypeContent(applicationContext, actionRepository,
                PomFilePluginActionClassName, applicationUtils);
        String expectedPropertiesString = "    <properties>\n" + PROPERTIES_STRING + "    </properties>\n";
        // Test
        Context velocityContext = this.sutSpy.initializeVelocityContext(applicationContext, actionRepository, propertyRepository, applicationUtils);
        // Verify
        assertThat(velocityContext.get(PROJECT_NAME_STRING_KEY)).isEqualTo(PROJECT_NAME);
        assertThat(velocityContext.get(PROGRAM_NAME_USED_IN_PRINT_VERSION_STRING_KEY)).isEqualTo(PROGRAM_NAME_USED_IN_PRINT_VERSION);
        assertThat(velocityContext.get(PACKAGE_NAME_STRING_KEY)).isEqualTo(PACKAGE_NAME);
        assertThat(velocityContext.get(ARTIFACT_ID_STRING_KEY)).isEqualTo(ARTIFACT_ID);
        assertThat(velocityContext.get(GROUPR_ID_STRING_KEY)).isEqualTo(GROUPR_ID);
        assertThat(velocityContext.get(VERSION_OF_APPLICATION_STRING_KEY)).isEqualTo(VERSION_OF_APPLICATION);
        assertThat(velocityContext.get(DEPENDECIES_STRING_STRING_KEY)).isEqualTo(DEPENDECIES_STRING);
        assertThat(velocityContext.get(PROPERTIES_STRING_STRING_KEY)).isEqualTo(expectedPropertiesString);
        assertThat(velocityContext.get(PLUGINS_STRING_STRING_KEY)).isEqualTo(PLUGINS_STRING);
    }


    @Test
    void extractDependencies() {
        // Initialize
        doReturn(dependencyContent).when(sutSpy).extractSpecificActionContent(any(), any(), any(), any());
        List<String> actionList = new LinkedList<>();
        actionList.add(action1);
        actionList.add(action2);
        ApplicationType applicationType = new ApplicationType("NAME", "DESCRIPTION", actionList);
        when(applicationContext.getApplicationType()).thenReturn(applicationType);
        when(actionRepository.getAction(action1)).thenReturn(new PomFileDependencyAction(dependencyContent));
        when(actionRepository.getAction(action2)).thenReturn(new PomFileDependencyAction(dependencyContent));
        String expected = dependencyContent + dependencyContent;
        // Test
        String result = this.sutSpy.extractApplicationTypeContent(applicationContext, actionRepository,
                PomFileDependencyActionClassName, applicationUtils);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractSpecificActionContentWhereActionIsPomFileDependencyAction() {
        // Initialize
        Action action = new PomFileDependencyAction(dependencyContent);
        String expected = dependencyContent;
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                PomFileDependencyActionClassName, applicationUtils);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractSpecificActionContentWhereActionIsPomFilePropertyAction() {
        // Initialize
        Action action = new PomFilePropertyAction(dependencyContent);
        String expected = dependencyContent;
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                PomFilePropertyActionClassName, applicationUtils);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractSpecificActionContentWhereActionIsListOfAction() {
        // Initialize
        ListOfAction action = new ListOfAction();
        Action action1 = new PomFileDependencyAction(dependencyContent);
        action.addAction(action1);
        Action action2 = new PomFileDependencyAction(dependencyContent2);
        action.addAction(action2);
        String expected = dependencyContent + dependencyContent2;
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                PomFileDependencyActionClassName, applicationUtils);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractSpecificActionContentWhereActionIsFolderPathActionButExpectedIsPomFileDependencyAction() {
        // Initialize
        Action action = new FolderPathAction(folderPath, propertyName);
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                PomFileDependencyActionClassName, applicationUtils);
        // Verify
        assertThat(result).isBlank();
    }

    //
    // createProjectFolder
    //

    @Test
    void createProjectFolder(@TempDir Path tempDir) {
        // Initialize
        when(applicationContext.getProjectName()).thenReturn(artifactId);
        when(applicationContext.getUserDir()).thenReturn(tempDir.toString());
        // Test
        this.sut.createProjectFolder(applicationContext, applicationUtils, propertyRepository);
        // Verify
        verify(propertyRepository, times(2)).put(any(), any());
    }

    //
    // addFolderPropertiesToVelocityContext
    //

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsNotFolderPathActionOrListOfAction() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.NO;
        Action action = new PomFileDependencyAction(action1);
        VelocityContext velocityContext = new VelocityContext();
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
        verify(this.sutSpy, times(1)).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        verify(propertyRepository, never()).containsNotProperty(any());
        verify(propertyRepository, never()).put(any(), any());
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsFolderPathActionFirstTime() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.NO;
        Action action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CONTEXT_FOLDER_PATH_KEY_1, CONTEXT_FOLDER_PATH_VALUE);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(true);
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
        verify(this.sutSpy, times(1)).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        verify(propertyRepository, times(1)).put(any(), any());
        assertThat(velocityContext.get(FOLDER_PATH_KEY)).isEqualTo(FOLDER_PATH_ACTION_CONTENT_RESULT);
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsFolderPathActionSecondTime() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.NO;
        Action action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        VelocityContext velocityContext = new VelocityContext();
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(false);
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
        verify(this.sutSpy, times(1)).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        verify(propertyRepository, never()).put(any(), any());
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsFolderPathActionContainsMutipleVariables() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.YES;
        Action action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_2, FOLDER_PATH_KEY);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CONTEXT_FOLDER_PATH_KEY_1, CONTEXT_FOLDER_PATH_VALUE);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(true);
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
        verify(this.sutSpy, times(1)).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        verify(propertyRepository, never()).put(any(), any());
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsListOfActionContainingNotFolderPathAction() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.NO;
        ListOfAction action = new ListOfAction();
        action.addAction(new PomFileDependencyAction(action1));
        VelocityContext velocityContext = new VelocityContext();
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsListOfActionContainingFolderPathAction() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.NO;
        ListOfAction action = new ListOfAction();
        action.addAction(new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY));
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CONTEXT_FOLDER_PATH_KEY_1, CONTEXT_FOLDER_PATH_VALUE);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(true);
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void addFolderPropertiesToVelocityContextFromFolderPathActionWhereActionIsListOfActionContainingFolderPathAction2() {
        // Initialize
        Application.UNRESOLVED_PROPERTIES expected = Application.UNRESOLVED_PROPERTIES.YES;
        ListOfAction action = new ListOfAction();
        action.addAction(new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_2, FOLDER_PATH_KEY));
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CONTEXT_FOLDER_PATH_KEY_1, CONTEXT_FOLDER_PATH_VALUE);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(true);
        // Test
        Application.UNRESOLVED_PROPERTIES result = this.sutSpy.addFolderPropertiesToVelocityContextFromFolderPathAction(action, applicationContext, velocityContext, applicationUtils, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void addFolderPropertiesToVelocityContextWhereAllActionsReturnNoUnresolvedProperties() {
        // Initialize
        String name = "";
        String description = "";
        Collection<String> actionNames = List.of("a1", "a2", "a3");
        ApplicationType applicationType = new ApplicationType(name, description, actionNames);
        when(applicationContext.getApplicationType()).thenReturn(applicationType);
        when(actionRepository.getAction(any())).thenReturn(new PomFileDependencyAction(""));
        doReturn(Application.UNRESOLVED_PROPERTIES.NO).when(this.sutSpy).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        VelocityContext velocityContext = new VelocityContext();
        // Test
        this.sutSpy.addFolderPropertiesToVelocityContext(velocityContext, applicationContext, actionRepository, propertyRepository, applicationUtils);
        // Verify
    }

    @Test
    void addFolderPropertiesToVelocityContextWhereOneActionsReturnUnresolvedProperties() {
        // Initialize
        String name = "";
        String description = "";
        Collection<String> actionNames = List.of("a1", "a2", "a3");
        ApplicationType applicationType = new ApplicationType(name, description, actionNames);
        when(applicationContext.getApplicationType()).thenReturn(applicationType);
        when(actionRepository.getAction(any())).thenReturn(new PomFileDependencyAction(""));
        doReturn(Application.UNRESOLVED_PROPERTIES.NO, Application.UNRESOLVED_PROPERTIES.YES, Application.UNRESOLVED_PROPERTIES.NO).when(this.sutSpy).addFolderPropertiesToVelocityContextFromFolderPathAction(any(), any(), any(), any(), any());
        VelocityContext velocityContext = new VelocityContext();
        // Test
        this.sutSpy.addFolderPropertiesToVelocityContext(velocityContext, applicationContext, actionRepository, propertyRepository, applicationUtils);
        // Verify
    }

    //
    // createFoldersFromActionList
    //

    @Test
    void createFoldersFromActionWhereActionIsNotListOfActionOrFolderPathAction() {
        // Initialize
        Action action = new PomFileDependencyAction(action1);
        // Test
        this.sutSpy.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        // Verify
        verify(propertyRepository, never()).get(any());
        verify(propertyRepository, never()).put(any(), any());
    }

    @Test
    void createFoldersFromActionWhereActionIsFolderPathAction(@TempDir Path tempDir) {
        // Initialize
        FolderPathAction action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        when(propertyRepository.get(projectFolderPathPropertyName)).thenReturn(tempDir.toString());
        when(propertyRepository.get(FOLDER_PATH_KEY)).thenReturn(CONTEXT_FOLDER_PATH_VALUE);
        Path expectedPath = Paths.get(tempDir.toString(), CONTEXT_FOLDER_PATH_VALUE);
        // Test
        this.sutSpy.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        // Verify
        verify(propertyRepository, times(1)).put(eq(action.getPropertyName() + "Path"), any());
        assertThat(Files.exists(expectedPath)).isTrue();
    }

    @Test
    void createFoldersFromActionWhereActionIsListOfAction() {
        // Initialize
        ListOfAction action = new ListOfAction();
        action.addAction(new PomFileDependencyAction(action1));
        // Test
        this.sutSpy.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        // Verify
        verify(propertyRepository, never()).get(any());
        verify(propertyRepository, never()).put(any(), any());
    }

    @Test
    void createFoldersFromActionWhereActionIsFolderPathActionWithNonExistingKey(@TempDir Path tempDir) {
        // Initialize
        FolderPathAction action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(true);
        // Test
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            this.sut.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        }, "RuntimeException expected to be thrown.");
        // Verify
        assertThat(thrown.getMessage()).startsWith("Property name: '");
        assertThat(thrown.getMessage()).contains("is not stored in PropertyRepository.");
    }

    @Test
    void createFoldersFromActionWhereActionIsFolderPathActionWithKeyBlank(@TempDir Path tempDir) {
        // Initialize
        FolderPathAction action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(false);
        when(propertyRepository.get(FOLDER_PATH_KEY)).thenReturn(" ");
        // Test
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            this.sut.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        }, "RuntimeException expected to be thrown.");
        // Verify
        assertThat(thrown.getMessage()).startsWith("Property name: '");
        assertThat(thrown.getMessage()).contains("is stored in PropertyRepository as empty string: '");
    }

    @Test
    void createFoldersFromActionWhereActionIsFolderPathActionWithKeyContainingVariable(@TempDir Path tempDir) {
        // Initialize
        FolderPathAction action = new FolderPathAction(FOLDER_PATH_ACTION_CONTENT_1, FOLDER_PATH_KEY);
        when(propertyRepository.containsNotProperty(FOLDER_PATH_KEY)).thenReturn(false);
        when(propertyRepository.get(FOLDER_PATH_KEY)).thenReturn(FOLDER_PATH_ACTION_CONTENT_1);
        // Test
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            this.sut.createFoldersFromAction(action, applicationContext, applicationUtils, propertyRepository);
        }, "RuntimeException expected to be thrown.");
        // Verify
        assertThat(thrown.getMessage()).startsWith("Property name: '");
        assertThat(thrown.getMessage()).contains("is stored in PropertyRepository with variable name in it. See: '");
    }

    /*
    @Test
    void createFoldersFromActionList() {
        // Initialize
        // Test
        this.sut.createFoldersFromActionList(applicationContext, applicationUtils, actionRepository, propertyRepository);
        // Verify
        assertThat(result).isEqualTo(expected);
    }
     */

    //
    // createFilesFromActionList
    //

    @Test
    void processVelocityTemplate(@TempDir Path tempDir) throws IOException {
        // Initialize
        String targetFileName = "Application.java";
        String templateName = "Application.vtl";
        // Test
        this.sut.processVelocityTemplate(targetFileName, templateName, tempDir, applicationContext, context, applicationUtils);
        // Verify
        Path pathToApplicationJava = Paths.get(tempDir.toString(), targetFileName);
        assertThat(Files.exists(pathToApplicationJava)).isTrue();
        String content = FileUtils.readFileToString(pathToApplicationJava.toFile(), StandardCharsets.UTF_8);
        assertThat(content).contains("System.out.println(\"" + programNameUsedInPrintVersion + ". Version: ");
        assertThat(content).contains("System.out.println(\"Welcome to " + programNameUsedInPrintVersion + "!\");");
    }

    static VelocityContext initializeVelocityForUnitTests() {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put( "programNameUsedInPrintVersion", programNameUsedInPrintVersion);
        return context;
    }
}
