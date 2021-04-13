package org.teinelund.freshmavenproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class ApplicationTest {

    private Application sut = null;

    @BeforeEach
    void init(TestInfo testInfo) {
        this.sut = new Application();
    }

    @Test
    void executeWhereArgsContainsHelpOption() {
        // Initialize
        String[] args = {"--help"};
        // Test
        this.sut.execute(args);
        // Verify
    }
}


