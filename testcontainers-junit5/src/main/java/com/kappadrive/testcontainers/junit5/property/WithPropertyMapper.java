package com.kappadrive.testcontainers.junit5.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides Mapping from container metadata to patterns
 * that can be used in user defined properties via {@link MapToSystemProperty}.
 *
 * @see MapToSystemProperty
 * @see PropertyResolver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WithPropertyMapper {
    /**
     * List of all {@link PropertyResolver} which should be used for property mapping.
     *
     * @return list of all resolvers to be used.
     */
    Class<? extends PropertyResolver<?>>[] value();
}
