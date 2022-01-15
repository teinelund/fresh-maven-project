package org.teinelund.freshmavenproject;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationTest {

    private Application sut = null;
    private static final String expectedVersionOfProject = "1.0.0-SNAPSHOT";
    private static final String artifactId = "PROJECT_1";
    static final String dependencyContent = "CONTENT";
    private static final String dependencyContent2 = "CONTENT2";
    private static final String folderPath = "FOLDER_PATH";
    private static final String propertyName = "PROPERTY_NAME";
    private static final String action1 = "ACTION_1";
    private static final String action2 = "ACTION_2";
    private static Context context;
    private static final String programNameUsedInPrintVersion = "PROGRAM_NAME";

    @Mock
    private CommandLineOptions options;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ApplicationUtils applicationUtils;

    @Mock
    private ActionRepository actionRepository;

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

    @Test
    void replaceMinusAndUnderscore() {
        // Initialize
        String text = "org.teinelund.order_engine" + "." + "test_2";
        String expected = "org.teinelund.orderengine.test2";
        // Test
        String result = this.sut.replaceMinusAndUnderscore(text);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsEmpty() {
        // Initialize
        String[] args = {};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertFalse(options.isVerbose());
        assertFalse(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsHelpShort() {
        // Initialize
        String[] args = {"-h"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertFalse(options.isVerbose());
        assertFalse(options.isVersion());
        assertTrue(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsHelpLong() {
        // Initialize
        String[] args = {"--help"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertFalse(options.isVerbose());
        assertFalse(options.isVersion());
        assertTrue(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsVersionShort() {
        // Initialize
        String[] args = {"-V"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertFalse(options.isVerbose());
        assertTrue(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsVersionLong() {
        // Initialize
        String[] args = {"--version"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertFalse(options.isVerbose());
        assertTrue(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsVerboseShort() {
        // Initialize
        String[] args = {"-v"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertTrue(options.isVerbose());
        assertFalse(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsVerboseLong() {
        // Initialize
        String[] args = {"--verbose"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertFalse(options.isInteractive());
        assertTrue(options.isVerbose());
        assertFalse(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsInteractiveShort() {
        // Initialize
        String[] args = {"-i"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertTrue(options.isInteractive());
        assertFalse(options.isVerbose());
        assertFalse(options.isVersion());
        assertFalse(options.isHelp());
    }

    @Test
    void parseCommandLineOptionsWhereArgsIsInteractiveLong() {
        // Initialize
        String[] args = {"--interactive"};
        CommandLineOptions options = new CommandLineOptions();
        // Test
        this.sut.parseCommandLineOptions(args, options);
        // Verify
        assertThat(options.getGroupid()).isBlank();
        assertThat(options.getArtifactId()).isBlank();
        assertThat(options.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(options.getProjectName()).isBlank();
        assertThat(options.getPackageName()).isBlank();
        assertFalse(options.isNoGit());
        assertTrue(options.isInteractive());
        assertFalse(options.isVerbose());
        assertFalse(options.isVersion());
        assertFalse(options.isHelp());
    }

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

    @Test
    void createProjectFolder(@TempDir Path tempDir) {
        // Initialize
        when(applicationContext.getArtifactId()).thenReturn(artifactId);
        when(applicationContext.getProjectName()).thenReturn(null);
        when(applicationContext.getUserDir()).thenReturn(tempDir.toString());
        // Test
        this.sut.createProjectFolder(applicationContext);
        // Verify
        verify(applicationContext, times(1)).setProjectFolder(any());
    }

    @Test
    void extractSpecificActionContentWhereActionIsPomFileDependencyAction() {
        // Initialize
        Action action = new PomFileDependencyAction(dependencyContent);
        String expected = dependencyContent;
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                Application.PomFileDependencyActionClassName);
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
                Application.PomFilePropertyActionClassName);
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
                Application.PomFileDependencyActionClassName);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractSpecificActionContentWhereActionIsFolderPathActionButExpectedIsPomFileDependencyAction() {
        // Initialize
        Action action = new FolderPathAction(folderPath, propertyName);
        // Test
        String result = this.sut.extractSpecificActionContent(action, applicationContext,
                Application.PomFileDependencyActionClassName);
        // Verify
        assertThat(result).isBlank();
    }

    @Test
    void extractDependencies() {
        // Initialize
        doReturn(dependencyContent).when(sutSpy).extractSpecificActionContent(any(), any(), any());
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
                Application.PomFileDependencyActionClassName);
        // Verify
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void initializeVelocity() {
        // Initialize
        doReturn(dependencyContent).when(sutSpy).extractApplicationTypeContent(any(), any(), any());
        // Test
        Context velocityContext = this.sutSpy.initializeVelocity(applicationContext, actionRepository);
        // Verify
        assertThat(velocityContext).isNotNull();
    }

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
