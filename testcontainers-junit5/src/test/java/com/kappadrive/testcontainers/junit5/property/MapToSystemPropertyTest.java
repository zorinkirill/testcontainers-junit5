package com.kappadrive.testcontainers.junit5.property;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import com.kappadrive.testcontainers.junit5.TestContainerFactory;
import com.kappadrive.testcontainers.junit5.WithTestContainerSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;

class MapToSystemPropertyTest {

    @BeforeEach
    void initNewContainers() {
        DummyContainerFactory.container = Mockito.mock(GenericContainer.class);
    }

    @Test
    void testClassLevelAnnotations() {
        EngineExecutionResults results = executeTests(selectClass(ClassLevelAnnotationsTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    @WithPropertyMapper(DummyPropertyResolver.class)
    @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}")
    static class ClassLevelAnnotationsTestCase {

        @Test
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("dummyValue");
        }

    }

    @Test
    void testMethodLevelAnnotations() {
        EngineExecutionResults results = executeTests(selectClass(MethodLevelAnnotationsTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class MethodLevelAnnotationsTestCase {

        @Test
        @WithDummyContainer
        @WithPropertyMapper(DummyPropertyResolver.class)
        @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}")
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("dummyValue");
        }

    }

    @Test
    void testMixedLevelAnnotations() {
        EngineExecutionResults results = executeTests(selectClass(MixedLevelAnnotationsTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    @WithPropertyMapper(DummyPropertyResolver.class)
    static class MixedLevelAnnotationsTestCase {

        @Test
        @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}")
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("dummyValue");
        }

    }

    @Test
    void testWrongOrderMixedLevelAnnotations() {
        EngineExecutionResults results = executeTests(selectClass(WrongOrderMixedLevelAnnotationsTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    @WithDummyContainer
    @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}")
    static class WrongOrderMixedLevelAnnotationsTestCase {

        @Test
        @WithPropertyMapper(DummyPropertyResolver.class)
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("${dummyKey}");
        }

    }

    @Test
    void testCombinedProperty() {
        EngineExecutionResults results = executeTests(selectClass(CombinedPropertyTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class CombinedPropertyTestCase {

        @Test
        @WithDummyContainer
        @WithPropertyMapper({DummyPropertyResolver.class, AnotherPropertyResolver.class})
        @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}-${anotherKey}")
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("dummyValue-anotherValue");
        }

    }

    @Test
    void testUnsupportedClass() {
        EngineExecutionResults results = executeTests(selectClass(CombinedPropertyTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class UnsupportedClassTestCase {

        @Test
        @WithDummyContainer
        @WithPropertyMapper(StringPropertyResolver.class)
        @MapToSystemProperty(container = "dummy", property = "test.property", value = "${stringKey}")
        void test() {
            assertThat(System.getProperty("test.property")).isEqualTo("${stringKey}");
        }

    }

    @Test
    void testNotExistingContainer() {
        EngineExecutionResults results = executeTests(selectClass(NotExistingContainerTestCase.class));

        results.testEvents().assertThatEvents().haveExactly(1,
            event(test("test"), finishedWithFailure(
                instanceOf(JUnitException.class),
                message("Failed to set property 'test.property' cause container not found: dummy")
            )));
    }

    static class NotExistingContainerTestCase {

        @Test
        @WithPropertyMapper(DummyPropertyResolver.class)
        @MapToSystemProperty(container = "dummy", property = "test.property", value = "${dummyKey}")
        void test() {
            // skipped
        }

    }

    private static class StringPropertyResolver implements PropertyResolver<String> {

        private static final Pattern PATTERN = Pattern.compile("\\$\\{stringKey}");

        @Override
        public Pattern getPattern() {
            return PATTERN;
        }

        @Override
        public Function<MatchResult, String> resolve(String obj) {
            return res -> "stringValue";
        }
    }

    private static class AnotherPropertyResolver implements PropertyResolver<Object> {

        private static final Pattern PATTERN = Pattern.compile("\\$\\{anotherKey}");

        @Override
        public Pattern getPattern() {
            return PATTERN;
        }

        @Override
        public Function<MatchResult, String> resolve(Object obj) {
            return res -> "anotherValue";
        }
    }

    private static class DummyPropertyResolver implements PropertyResolver<Object> {

        private static final Pattern PATTERN = Pattern.compile("\\$\\{dummyKey}");

        @Override
        public Pattern getPattern() {
            return PATTERN;
        }

        @Override
        public Function<MatchResult, String> resolve(Object obj) {
            return res -> "dummyValue";
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
