package com.kappadrive.testcontainers.junit5;

import com.kappadrive.testcontainers.junit5.property.HostPropertyResolver;
import com.kappadrive.testcontainers.junit5.property.PortPropertyResolver;
import com.kappadrive.testcontainers.junit5.property.WithPropertyMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Main annotation to create test containers which can be used in tests.
 * Should be used in composition with {@link TestContainerFactory} and custom annotation like:
 * <pre>{@code
 * {@literal @}WithTestContainerSupport(MyCustomTestContainerFactory.class)
 * public @interface WithMyTestContainer {
 *     // some methods to parameterize factory
 * }
 * }</pre>
 *
 * <p>By design, source annotation could be later used directly (or indirectly via additional annotations) on test:
 * <pre>{@code
 * {@literal @}WithMyTestContainer("some")
 * void Tests {
 *     {@literal @}Test
 *     void test() {
 *         ...
 *     }
 * }
 * }</pre>
 * or
 * <pre>{@code
 * void Tests {
 *     {@literal @}WithMyTestContainer("some")
 *     {@literal @}Test
 *     void test() {
 *         ...
 *     }
 * }
 * }</pre>
 *
 * <p>Custom source annotation could use any child of {@link org.testcontainers.containers.GenericContainer}
 * and it is recommended to provide mapping custom container data (like usernames, urls, ports...) via {@link WithPropertyMapper}.
 * However, this annotation could be also present directly on test class/method.
 *
 * <p>Depending on container nature, custom annotation could be annotated with {@link java.lang.annotation.Repeatable}
 * in order to support multiple containers based on same factory and annotation type.
 * It will require some parametrization, at least for container name.
 *
 * @see TestContainerFactory
 * @see WithPropertyMapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@WithPropertyMapper({HostPropertyResolver.class, PortPropertyResolver.class})
@ExtendWith(TestContainersExtension.class)
public @interface WithTestContainerSupport {

    /**
     * Returns factory which will create test container.
     *
     * @return factory which will create test container.
     */
    Class<? extends TestContainerFactory> value();
}
