package org.teinelund.freshmavenproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CommandLineOptionsTest {

    private CommandLineOptions sut;

    private static final String expectedVersionOfProject = "1.0.0-SNAPSHOT";

    @BeforeEach
    void init(TestInfo testInfo) {
        this.sut = new CommandLineOptions();
    }

    @Test
    void parseWhereArgsIsEmpty() {
        // Initialize
        String[] args = {};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsHelpShort() {
        // Initialize
        String[] args = {"-h"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertTrue(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsHelpLong() {
        // Initialize
        String[] args = {"--help"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertTrue(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsVersionShort() {
        // Initialize
        String[] args = {"-V"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertTrue(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsVersionLong() {
        // Initialize
        String[] args = {"--version"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertTrue(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsVerboseShort() {
        // Initialize
        String[] args = {"-v"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertTrue(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsVerboseLong() {
        // Initialize
        String[] args = {"--verbose"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertFalse(sut.isInteractive());
        assertTrue(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsInteractiveShort() {
        // Initialize
        String[] args = {"-i"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertTrue(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertFalse(sut.isHelp());
    }

    @Test
    void parseCommandLinesutWhereArgsIsInteractiveLong() {
        // Initialize
        String[] args = {"--interactive"};
        // Test
        this.sut.parse(args);
        // Verify
        assertThat(sut.getGroupid()).isBlank();
        assertThat(sut.getArtifactId()).isBlank();
        assertThat(sut.getVersionOfProject()).isEqualTo(expectedVersionOfProject);
        assertThat(sut.getProjectName()).isBlank();
        assertThat(sut.getPackageName()).isBlank();
        assertFalse(sut.isNoGit());
        assertTrue(sut.isInteractive());
        assertFalse(sut.isVerbose());
        assertFalse(sut.isVersion());
        assertFalse(sut.isHelp());
    }
}
