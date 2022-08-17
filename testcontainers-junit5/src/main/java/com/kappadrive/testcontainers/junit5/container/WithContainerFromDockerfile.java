package com.kappadrive.testcontainers.junit5.container;

import com.kappadrive.testcontainers.junit5.WithTestContainerSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates simple container which will be built from Dockerfile and additional resources from classpath.
 * Will use docker <code>HEALTHCHECK</code> command to wait for container start.
 * Supports multiple annotations on same class.
 * Note: due to implementation in Test Containers and java Resource - all resources for container should be
 * either in main classpath or in test. If there are any resources in test - main will be completely ignored.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(WithContainersFromDockerfile.class)
@WithTestContainerSupport(ContainerFromDockerfileFactory.class)
public @interface WithContainerFromDockerfile {

    /**
     * Name of the container.
     *
     * @return container name.
     */
    String value();

    /**
     * Returns path to resources in classpath.
     * By default it matches container name.
     * Note: unfortunately, it is impossible to map root classpath dir "/".
     *
     * @return resources in classpath to be copied to docker build.
     */
    String resourcePath() default "";

    /**
     * Ports that should be exposed by container.
     * Empty by default.
     *
     * @return ports that should be exposed.
     */
    int[] exposedPort() default {};
}
