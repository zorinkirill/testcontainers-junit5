package com.kappadrive.testcontainers.junit5;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;

class AnnotationConsumerTest {

    @BeforeEach
    void initNewContainer() {
        DummyContainerFactory.CONTAINER_MAP.clear();
    }

    @Test
    void testAnnotationIsConsumed() {
        EngineExecutionResults results = executeTests(selectMethod(TestCases.class, "testConsumed"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("testConsumed"), finishedSuccessfully()));

        assertThat(DummyContainerFactory.CONTAINER_MAP)
            .containsOnlyKeys("dummy");
    }

    @Test
    void testIncorrectAnnotation() {
        EngineExecutionResults results = executeTests(selectMethod(TestCases.class, "testIncorrectAnnotation"));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("testIncorrectAnnotation"), finishedWithFailure(
                instanceOf(JUnitException.class),
                message("DummyContainerFactory implementing AnnotationConsumer<WithDummyContainer>"
                    + " but is a meta annotation for @WithInvalidContainer"))));
    }

    static class TestCases {

        @Test
        @WithDummyContainer("dummy")
        void testConsumed() {
            // skipped
        }

        @Test
        @WithInvalidContainer("dummy")
        void testIncorrectAnnotation() {
            // skipped
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @WithTestContainerSupport(DummyContainerFactory.class)
    private @interface WithDummyContainer {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @WithTestContainerSupport(DummyContainerFactory.class)
    private @interface WithInvalidContainer {
        String value();
    }

    private static class DummyContainerFactory implements TestContainerFactory, AnnotationConsumer<WithDummyContainer> {

        private static final Map<String, GenericContainer<?>> CONTAINER_MAP = new HashMap<>();

        private WithDummyContainer withDummyContainer;

        @Override
        public void accept(WithDummyContainer withDummyContainer) {
            this.withDummyContainer = withDummyContainer;
        }

        @Override
        public String getContainerName() {
            return withDummyContainer.value();
        }

        @Override
        public GenericContainer<?> createContainer() {
            return CONTAINER_MAP.computeIfAbsent(getContainerName(), k -> Mockito.mock(GenericContainer.class));
        }
    }
}
