package org.teinelund.freshmavenproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {

    private Application sut = null;

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
}


