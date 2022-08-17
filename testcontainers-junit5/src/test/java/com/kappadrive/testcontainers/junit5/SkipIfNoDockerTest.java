package com.kappadrive.testcontainers.junit5;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.mockito.BDDMockito.given;

import com.kappadrive.testcontainers.junit5.extension.WithMockedDockerClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.testcontainers.DockerClientFactory;

@WithMockedDockerClientFactory
class SkipIfNoDockerTest {

    @Test
    void testDockerIsAvailable(DockerClientFactory factory) {
        given(factory.isDockerAvailable()).willReturn(true);

        EngineExecutionResults results = executeTests(selectMethod(TestCase.class, "test"));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedSuccessfully()));
    }

    @Test
    void testDockerIsNotAvailable(DockerClientFactory factory) {
        given(factory.isDockerAvailable()).willReturn(false);

        EngineExecutionResults results = executeTests(selectMethod(TestCase.class, "test"));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), skippedWithReason("Docker is not available")));
    }

    @Test
    void testDockerFailingToInit(DockerClientFactory factory) {
        given(factory.isDockerAvailable()).willThrow(new RuntimeException("Some internal exception"));

        EngineExecutionResults results = executeTests(selectMethod(TestCase.class, "test"));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(cause(
                instanceOf(RuntimeException.class),
                message("Some internal exception")
            ))));
    }

    static class TestCase {

        @Test
        @SkipIfNoDocker
        void test() {
            // skipped
        }
    }
}
