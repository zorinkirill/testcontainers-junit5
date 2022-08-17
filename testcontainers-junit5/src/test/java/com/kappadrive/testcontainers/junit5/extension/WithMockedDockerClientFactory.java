package com.kappadrive.testcontainers.junit5.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Helper annotation to mock docker client behavior without actually calling it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(MockDockerClientFactoryExtension.class)
public @interface WithMockedDockerClientFactory {
}
