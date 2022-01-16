package org.teinelund.freshmavenproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InteractiveQueryEngineTest {

    private InteractiveQueryEngine sut = null;

    @BeforeEach
    void init(TestInfo testInfo) {
        this.sut = new InteractiveQueryEngine();
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
