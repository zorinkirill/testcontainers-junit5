package com.kappadrive.testcontainers.junit5.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Support to allow multiple {@link MapToSystemProperty} on same test/annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MapToSystemProperties {

    /**
     * Returns list of property mappers.
     *
     * @return list of property mappers.
     */
    MapToSystemProperty[] value();
}
