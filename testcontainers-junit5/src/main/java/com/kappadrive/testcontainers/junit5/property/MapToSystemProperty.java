package com.kappadrive.testcontainers.junit5.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Sets system property with value based on container metadata.
 * Should be used in order to link started container with tested application:
 * <pre>{@code
 * {@literal @}WithMyTestContainer(value = "some", exposedPort = 9999)
 * {@literal @}MapToSystemProperty(container = "some", property = "my.connection.host", value = "${host}")
 * {@literal @}MapToSystemProperty(container = "some", property = "my.connection.port", value = "${port:9999}")
 * {@literal @}SpringBootTest // or another properties consumer
 * void MyTest {
 *     ...
 * }
 * }</pre>
 *
 * <p>Property values will mapped taking into account all {@link WithPropertyMapper} available for test.
 * Note: will be used only {@link PropertyResolver} which are based on supertypes for provided container.
 * Note: must be annotated at same level as
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(MapToSystemProperties.class)
@ExtendWith(MapToSystemPropertyExtension.class)
public @interface MapToSystemProperty {

    /**
     * Returns container where property should be taken from.
     *
     * @return container where property should be taken from.
     */
    String container();

    /**
     * Returns system property name which should be set with mapped value.
     *
     * @return system property name which should be set with mapped value.
     */
    String property();

    /**
     * Returns property value pattern, which will be mapped with values from container.
     *
     * @return property value pattern.
     */
    String value();
}
