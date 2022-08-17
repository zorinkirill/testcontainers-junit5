package com.kappadrive.testcontainers.junit5.container;

import com.kappadrive.testcontainers.junit5.AnnotationConsumer;
import com.kappadrive.testcontainers.junit5.TestContainerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * Simple factory creating container from Dockerfile and other resources.
 */
class ContainerFromDockerfileFactory implements TestContainerFactory, AnnotationConsumer<WithContainerFromDockerfile> {

    private WithContainerFromDockerfile withContainerFromDockerfile;

    @Override
    public void accept(WithContainerFromDockerfile withContainerFromDockerfile) {
        this.withContainerFromDockerfile = withContainerFromDockerfile;
    }

    @Override
    public String getContainerName() {
        checkIsConfigured();
        return withContainerFromDockerfile.value();
    }

    @Override
    public GenericContainer<?> createContainer() {
        checkIsConfigured();
        String resourcePath = withContainerFromDockerfile.resourcePath().isEmpty()
            ? withContainerFromDockerfile.value()
            : withContainerFromDockerfile.resourcePath();
        return new GenericContainer<>(
            new ImageFromDockerfile().withFileFromClasspath("/", resourcePath)
        )
            .withExposedPorts(toIntegerArray(withContainerFromDockerfile.exposedPort()))
            .waitingFor(Wait.forHealthcheck());
    }

    private void checkIsConfigured() {
        if (withContainerFromDockerfile == null) {
            throw new IllegalStateException("Factory is not yet configured");
        }
    }

    private static Integer[] toIntegerArray(int[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }
}
