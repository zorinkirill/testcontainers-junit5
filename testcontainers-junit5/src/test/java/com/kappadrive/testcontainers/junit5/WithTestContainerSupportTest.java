package com.kappadrive.testcontainers.junit5;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.testcontainers.containers.GenericContainer;

class WithTestContainerSupportTest {

    @BeforeEach
    void initNewContainers() {
        DummyContainerFactory.container = Mockito.mock(GenericContainer.class);
        AnotherDummyContainerFactory.container = Mockito.mock(GenericContainer.class);
    }

    @Test
    void testMethodLevelSingleTest() {
        EngineExecutionResults results = executeTests(selectMethod(MethodLevelTestCase.class, "test1"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
    }

    @Test
    void testMethodLevelMultipleTests() {
        EngineExecutionResults results = executeTests(selectClass(MethodLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testFailedToStopContainer(CapturedOutput output) {
        willThrow(new RuntimeException("Some error")).given(DummyContainerFactory.container).stop();

        EngineExecutionResults results = executeTests(selectClass(MethodLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();

        assertThat(output)
            .contains("ERROR com.kappadrive.testcontainers.junit5.TestContainers - Failed to stop container dummy")
            .contains("java.lang.RuntimeException: Some error");
    }

    static class MethodLevelTestCase {

        @Test
        @WithDummyContainer
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

        @Test
        @WithDummyContainer
        void test2() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

    }

    @Test
    void testClassLevelSingleTest() {
        EngineExecutionResults results = executeTests(selectMethod(ClassLevelTestCase.class, "test1"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
    }

    @Test
    void testClassLevelMultipleTests() {
        EngineExecutionResults results = executeTests(selectClass(ClassLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
    }

    @WithDummyContainer
    static class ClassLevelTestCase {

        @Test
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

        @Test
        void test2() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

    }

    @Test
    void testBothLevels() {
        EngineExecutionResults results = executeTests(selectClass(BothLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
    }

    @WithDummyContainer
    static class BothLevelTestCase {

        @Test
        @WithDummyContainer
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

    }

    @Test
    void testMultipleContainersClassLevel() {
        EngineExecutionResults results = executeTests(selectClass(MultipleContainersClassLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(AnotherDummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
        then(AnotherDummyContainerFactory.container).should(times(1)).stop();
    }

    @WithDummyContainer
    @WithAnotherDummyContainer
    static class MultipleContainersClassLevelTestCase {

        @Test
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
            verify(AnotherDummyContainerFactory.container, times(1)).start();
        }

        @Test
        void test2() {
            verify(DummyContainerFactory.container, times(1)).start();
            verify(AnotherDummyContainerFactory.container, times(1)).start();
        }

    }

    @Test
    void testDifferentContainersOnTestsMultipleTests() {
        EngineExecutionResults results = executeTests(selectClass(DifferentContainersMethodLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(AnotherDummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
        then(AnotherDummyContainerFactory.container).should(times(1)).stop();
    }

    @Test
    void testDifferentContainersOnTestsSingleTest() {
        EngineExecutionResults results = executeTests(selectMethod(DifferentContainersMethodLevelTestCase.class, "test1"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(AnotherDummyContainerFactory.container).should(never()).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
        then(AnotherDummyContainerFactory.container).should(never()).stop();
    }

    static class DifferentContainersMethodLevelTestCase {

        @Test
        @WithDummyContainer
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

        @Test
        @WithAnotherDummyContainer
        void test2() {
            verify(AnotherDummyContainerFactory.container, times(1)).start();
        }

    }

    @Test
    void testMultipleContainersMixedLevels() {
        EngineExecutionResults results = executeTests(selectClass(MultipleContainersMixedTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test1"), finishedSuccessfully()))
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(AnotherDummyContainerFactory.container).should(times(1)).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
        then(AnotherDummyContainerFactory.container).should(times(1)).stop();
    }

    @Test
    void testOnlyOneContainerStartedOnSingleTest() {
        EngineExecutionResults results = executeTests(selectMethod(MultipleContainersMixedTestCase.class, "test2"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test2"), finishedSuccessfully()));

        then(DummyContainerFactory.container).should(times(1)).start();
        then(AnotherDummyContainerFactory.container).should(never()).start();
        then(DummyContainerFactory.container).should(times(1)).stop();
        then(AnotherDummyContainerFactory.container).should(never()).stop();
    }

    @WithDummyContainer
    static class MultipleContainersMixedTestCase {

        @Test
        @WithAnotherDummyContainer
        void test1() {
            verify(DummyContainerFactory.container, times(1)).start();
            verify(AnotherDummyContainerFactory.container, times(1)).start();
        }

        @Test
        void test2() {
            verify(DummyContainerFactory.container, times(1)).start();
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @WithTestContainerSupport(DummyContainerFactory.class)
    private @interface WithDummyContainer {
    }

    private static class DummyContainerFactory implements TestContainerFactory {

        private static GenericContainer<?> container;

        @Override
        public String getContainerName() {
            return "dummy";
        }

        @Override
        public GenericContainer<?> createContainer() {
            return container;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @WithTestContainerSupport(AnotherDummyContainerFactory.class)
    private @interface WithAnotherDummyContainer {
    }

    private static class AnotherDummyContainerFactory implements TestContainerFactory {

        private static GenericContainer<?> container;

        @Override
        public String getContainerName() {
            return "another";
        }

        @Override
        public GenericContainer<?> createContainer() {
            return container;
        }
    }
}
