package com.kappadrive.testcontainers.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to inject Test Container from Jupiter test context into field or parameter.
 * This annotation is required injecting directly to a field:
 * <pre>{@code
 * {@literal @}Container("database")
 * private GenericContainer{@code <?>} database;
 * }</pre>
 * Injection in static fields is also allowed:
 * <pre>{@code
 *  * {@literal @}Container("database")
 *  * private static GenericContainer{@code <?>} database;
 *  * }</pre>
 *
 * <p>Or if parameter name is different from container name:
 * <pre>{@code
 * private GenericContainer{@code <?>} db;
 * void MyTest({@literal @}Container("database") GenericContainer{@code <?>} db) {
 *     this.db = db;
 * }
 * }</pre>
 * or
 * <pre>{@code
 * {@literal @}Test
 * void test({@literal @}Container("database") GenericContainer{@code <?>} db) {
 *     ...
 * }
 * }</pre>
 *
 * <p>If Container name matches parameter name in last two cases - this annotation might be skipped.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Container {

    /**
     * Name of the test container to be found and injected.
     *
     * @return name of the test container.
     */
    String value();
}
