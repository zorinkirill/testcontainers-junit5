package com.kappadrive.testcontainers.junit5;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
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
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;

class ContainerTest {

    @BeforeEach
    void initNewContainers() {
        DummyContainerFactory.container = Mockito.mock(GenericContainer.class);
    }

    @Test
    void testByMethodParameterName() {
        EngineExecutionResults results = executeTests(selectClass(ByMethodParameterNameTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class ByMethodParameterNameTestCase {

        @Test
        @WithDummyContainer
        void test(GenericContainer<?> dummy) {
            assertThat(dummy).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testInvalidMethodParameterName() {
        EngineExecutionResults results = executeTests(selectClass(InvalidMethodParameterNameTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [org.testcontainers.containers.GenericContainer<?> invalid] "
                        + "in method [void com.kappadrive.testcontainers.junit5.ContainerTest$InvalidMethodParameterNameTestCase.test"
                        + "(org.testcontainers.containers.GenericContainer<?>)].")
            )));
    }

    static class InvalidMethodParameterNameTestCase {

        @Test
        @WithDummyContainer
        void test(GenericContainer<?> invalid) {
            // skipped
        }

    }

    @Test
    void testByMethodAnnotationValue() {
        EngineExecutionResults results = executeTests(selectClass(ByMethodAnnotationValueTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class ByMethodAnnotationValueTestCase {

        @Test
        @WithDummyContainer
        void test(@Container("dummy") GenericContainer<?> container) {
            assertThat(container).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testInvalidMethodAnnotationValue() {
        EngineExecutionResults results = executeTests(selectClass(InvalidMethodAnnotationValueTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [org.testcontainers.containers.GenericContainer<?> container] "
                        + "in method [void com.kappadrive.testcontainers.junit5.ContainerTest$InvalidMethodAnnotationValueTestCase.test"
                        + "(org.testcontainers.containers.GenericContainer<?>)].")
            )));
    }

    static class InvalidMethodAnnotationValueTestCase {

        @Test
        @WithDummyContainer
        void test(@Container("invalid") GenericContainer<?> container) {
            // skipped
        }

    }

    @Test
    void testMethodSuperClass() {
        EngineExecutionResults results = executeTests(selectClass(MethodSuperClassTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class MethodSuperClassTestCase {

        @Test
        @WithDummyContainer
        void test(Object dummy) {
            assertThat(dummy).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testMethodIncorrectClass() {
        EngineExecutionResults results = executeTests(selectClass(MethodIncorrectClassTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [java.lang.String dummy] "
                        + "in method [void com.kappadrive.testcontainers.junit5.ContainerTest$MethodIncorrectClassTestCase.test"
                        + "(java.lang.String)].")
            )));
    }

    static class MethodIncorrectClassTestCase {

        @Test
        @WithDummyContainer
        void test(String dummy) {
            // skipped
        }

    }

    @Test
    void testByConstructorParameterName() {
        EngineExecutionResults results = executeTests(selectClass(ByConstructorParameterNameTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    @RequiredArgsConstructor
    static class ByConstructorParameterNameTestCase {

        private final GenericContainer<?> dummy;

        @Test
        void test() {
            assertThat(dummy).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testInvalidConstructorParameterName() {
        EngineExecutionResults results = executeTests(selectClass(InvalidConstructorParameterNameTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [final org.testcontainers.containers.GenericContainer<?> invalid] "
                        + "in constructor [public com.kappadrive.testcontainers.junit5.ContainerTest$InvalidConstructorParameterNameTestCase"
                        + "(org.testcontainers.containers.GenericContainer<?>)].")
            )));
    }

    @WithDummyContainer
    @RequiredArgsConstructor
    static class InvalidConstructorParameterNameTestCase {

        private final GenericContainer<?> invalid;

        @Test
        void test() {
            // skipped
        }

    }

    @Test
    void testByConstructorParameterNameMethodLevel() {
        EngineExecutionResults results = executeTests(selectClass(ByConstructorParameterNameMethodLevelTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [final org.testcontainers.containers.GenericContainer<?> dummy] "
                        + "in constructor [public com.kappadrive.testcontainers.junit5.ContainerTest$ByConstructorParameterNameMethodLevelTestCase"
                        + "(org.testcontainers.containers.GenericContainer<?>)].")
            )));
    }

    @RequiredArgsConstructor
    static class ByConstructorParameterNameMethodLevelTestCase {

        private final GenericContainer<?> dummy;

        @Test
        @WithDummyContainer
        void test() {
            // skipped
        }

    }

    @Test
    void testByConstructorAnnotationValue() {
        EngineExecutionResults results = executeTests(selectClass(ByConstructorParameterNameTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    static class ByConstructorAnnotationValueTestCase {

        private final GenericContainer<?> container;

        ByConstructorAnnotationValueTestCase(@Container("dummy") GenericContainer<?> container) {
            this.container = container;
        }

        @Test
        void test() {
            assertThat(container).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testInvalidConstructorAnnotationValue() {
        EngineExecutionResults results = executeTests(selectClass(InvalidConstructorAnnotationValueTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(ParameterResolutionException.class),
                message(
                    "No ParameterResolver registered for parameter [org.testcontainers.containers.GenericContainer<?> container] "
                        + "in constructor [com.kappadrive.testcontainers.junit5.ContainerTest$InvalidConstructorAnnotationValueTestCase"
                        + "(org.testcontainers.containers.GenericContainer<?>)].")
            )));
    }

    @WithDummyContainer
    static class InvalidConstructorAnnotationValueTestCase {

        InvalidConstructorAnnotationValueTestCase(@Container("invalid") GenericContainer<?> container) {
            // skipped
        }

        @Test
        void test() {
            // skipped
        }

    }

    @Test
    void testFieldInjectionMethodLevel() {
        EngineExecutionResults results = executeTests(selectClass(FieldInjectionMethodLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class FieldInjectionMethodLevelTestCase {

        @Container("dummy")
        private GenericContainer<?> container;

        @Test
        @WithDummyContainer
        void test() {
            assertThat(container).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testFieldInjectionInvalidName() {
        EngineExecutionResults results = executeTests(selectClass(FieldInjectionInvalidNameTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(JUnitException.class),
                message(
                    "Failed to set field private org.testcontainers.containers.GenericContainer "
                        + "com.kappadrive.testcontainers.junit5.ContainerTest$FieldInjectionInvalidNameTestCase.container "
                        + "cause container not found: invalid")
            )));
    }

    static class FieldInjectionInvalidNameTestCase {

        @Container("invalid")
        private GenericContainer<?> container;

        @Test
        @WithDummyContainer
        void test() {
            // skipped
        }

    }

    @Test
    void testFieldInjectionSuperClass() {
        EngineExecutionResults results = executeTests(selectClass(FieldInjectionSuperClassTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class FieldInjectionSuperClassTestCase {

        @Container("dummy")
        private Object container;

        @Test
        @WithDummyContainer
        void test() {
            assertThat(container).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testFieldInjectionInvalidClass() {
        EngineExecutionResults results = executeTests(selectClass(FieldInjectionInvalidClass.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(JUnitException.class),
                message(message -> message.matches(
                    "^Failed to insert container of type GenericContainer\\$MockitoMock\\$.+ into field private java\\.lang\\.String "
                        + "com\\.kappadrive\\.testcontainers\\.junit5\\.ContainerTest\\$FieldInjectionInvalidClass\\.container$"))
            )));
    }

    static class FieldInjectionInvalidClass {

        @Container("dummy")
        private String container;

        @Test
        @WithDummyContainer
        void test() {
            // skipped
        }

    }

    @Test
    void testFieldInjectionClassLevel() {
        EngineExecutionResults results = executeTests(selectClass(FieldInjectionClassLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    static class FieldInjectionClassLevelTestCase {

        @Container("dummy")
        private GenericContainer<?> container;

        @Test
        void test() {
            assertThat(container).isSameAs(DummyContainerFactory.container);
        }

    }

    @Test
    void testStaticFieldInjectionMethodLevel() {
        EngineExecutionResults results = executeTests(selectClass(StaticFieldInjectionMethodLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class StaticFieldInjectionMethodLevelTestCase {

        @Container("dummy")
        private static GenericContainer<?> container;

        @Test
        @WithDummyContainer
        void test() {
            assertThat(container).isNull();
        }

    }

    @Test
    void testStaticFieldInjectionClassLevel() {
        EngineExecutionResults results = executeTests(selectClass(StaticFieldInjectionClassLevelTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    static class StaticFieldInjectionClassLevelTestCase {

        @Container("dummy")
        private static GenericContainer<?> container;

        @Test
        void test() {
            assertThat(container).isSameAs(DummyContainerFactory.container);
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
}
