package com.kappadrive.testcontainers.junit5.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Support to allow multiple {@link WithContainerFromDockerfile} on same test/annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WithContainersFromDockerfile {

    /**
     * Returns list of dockerfile containers.
     *
     * @return list of dockerfile containers.
     */
    WithContainerFromDockerfile[] value();
}
