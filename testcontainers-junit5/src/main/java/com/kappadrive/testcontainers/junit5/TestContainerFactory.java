package com.kappadrive.testcontainers.junit5;

import org.testcontainers.containers.GenericContainer;

/**
 * Main factory class for Test Containers.
 * Should be used in composition with meta annotation {@link WithTestContainerSupport} and custom annotation like:
 * <pre>{@code
 * {@literal @}WithTestContainerSupport(MyCustomTestContainerFactory.class)
 * public @interface WithMyTestContainer {
 *     // some methods to parameterize factory
 * }
 * }</pre>
 *
 * <p>Could implement {@link AnnotationConsumer} to accept source annotation (like <code>WithMyTestContainer</code>)
 * for additional container parametrization.
 * In this case {@link AnnotationConsumer} MUST accept same source annotation type.
 * It is highly recommended to specify at least container name via <code>value()</code> (but not required).
 *
 * <p>Note: Using same Factory in multiple source annotation is not by design and is not compatible with {@link AnnotationConsumer}.
 *
 * @see WithTestContainerSupport
 * @see AnnotationConsumer
 */
public interface TestContainerFactory {

    /**
     * Returns unique created container name.
     * Container name will be used to identify if such container is already started or not
     * and to map into parameter or field with or without {@link Container}.
     *
     * @return unique created container name.
     * @see Container
     */
    String getContainerName();

    /**
     * Creates test container which will be later started and saved into Jupiter test context.
     *
     * @return created container.
     */
    GenericContainer<?> createContainer();
}
