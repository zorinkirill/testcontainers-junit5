package com.kappadrive.testcontainers.junit5.container;

import static com.kappadrive.testcontainers.junit5.TestKitUtil.executeTests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.test;

import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;

class ContainerFromDockerfileFactoryTest {

    @Test
    void testGetContainerNameNotConfigured() {
        var factory = new ContainerFromDockerfileFactory();

        assertThatThrownBy(factory::getContainerName)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Factory is not yet configured");
    }

    @Test
    void testGetContainerName() {
        var factory = factory(withContainerFromDockerfile("dummy", "", new int[0]));

        assertThat(factory.getContainerName()).isEqualTo("dummy");
    }

    @Test
    void testCreateContainerNotConfigured() {
        var factory = new ContainerFromDockerfileFactory();

        assertThatThrownBy(factory::createContainer)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Factory is not yet configured");
    }

    @Test
    void testContainerExposedPorts() {
        var factory = factory(withContainerFromDockerfile("dummy", "", new int[] {666, 777}));
        var container = factory.createContainer();

        assertThat(container.getExposedPorts())
            .containsExactlyInAnyOrder(666, 777);
    }

    @Test
    void testCreateContainerNotExistingResource() {
        var factory = factory(withContainerFromDockerfile("incorrect", "", new int[0]));

        assertThatThrownBy(factory::createContainer)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Resource with path incorrect could not be found on any of these classloaders:");
    }

    @Test
    void testCreateContainer() {
        EngineExecutionResults results = executeTests(selectClass(CreateContainerTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class CreateContainerTestCase {

        @Test
        @WithContainerFromDockerfile("dummy")
        void test() {
            // skipped
        }

    }

    @Test
    void testCreateContainerDifferentLocation() {
        EngineExecutionResults results = executeTests(selectClass(CreateContainerDifferentLocationTestCase.class));

        results.testEvents().assertThatEvents()
            .haveExactly(1, event(test("test"), finishedSuccessfully()));
    }

    static class CreateContainerDifferentLocationTestCase {

        @Test
        @WithContainerFromDockerfile(value = "container", resourcePath = "dummy")
        void test() {
            // skipped
        }

    }

    private static ContainerFromDockerfileFactory factory(WithContainerFromDockerfile withContainerFromDockerfile) {
        var factory = new ContainerFromDockerfileFactory();
        factory.accept(withContainerFromDockerfile);
        return factory;
    }

    private static WithContainerFromDockerfile withContainerFromDockerfile(final String value, final String resourcePath, final int[] exposedPort) {
        return new WithContainerFromDockerfile() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return WithContainerFromDockerfile.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String resourcePath() {
                return resourcePath;
            }

            @Override
            public int[] exposedPort() {
                return exposedPort;
            }
        };
    }
}
