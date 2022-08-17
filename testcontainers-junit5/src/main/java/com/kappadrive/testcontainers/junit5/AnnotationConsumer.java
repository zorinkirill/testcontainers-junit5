package com.kappadrive.testcontainers.junit5;

import java.lang.annotation.Annotation;

/**
 * Specifies that custom {@link TestContainerFactory} is using annotation for building Test Container.
 * Factory class MUST accept same annotation as directly annotated by {@link WithTestContainerSupport}.
 *
 * @param <A> - custom annotation which should be used for building.
 * @see TestContainerFactory
 * @see WithTestContainerSupport
 */
public interface AnnotationConsumer<A extends Annotation> {

    /**
     * Applies annotation configuration to factory.
     *
     * @param annotation - custom annotation to be applied.
     */
    void accept(A annotation);
}
