package com.kappadrive.testcontainers.junit5;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Methods to execute custom JUnit5 Jupiter test cases.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TestKitUtil {

    /**
     * Runs tests with jupiter engine.
     *
     * @param selectors - selectors for test engine.
     * @return results of execution.
     */
    public static EngineExecutionResults executeTests(DiscoverySelector... selectors) {
        return EngineTestKit.engine("junit-jupiter")
            .selectors(selectors)
            .execute();
    }

}
