package com.kappadrive.testcontainers.junit5;

import static com.kappadrive.testcontainers.junit5.TestContainers.getTestContainers;
import static java.util.Objects.requireNonNull;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.testcontainers.containers.GenericContainer;

@Log4j2
class TestContainersExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        initContainers(context, ReflectionUtils::isStatic);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        initContainers(context, ReflectionUtils::isNotStatic);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        TestContainers testContainers = getTestContainers(extensionContext);
        String containerName = getContainerName(parameterContext);
        return testContainers.contains(containerName)
            && parameterContext.getParameter().getType().isAssignableFrom(testContainers.get(containerName).getClass());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getTestContainers(extensionContext).get(getContainerName(parameterContext));
    }

    private static void initContainers(ExtensionContext context, Predicate<Field> fieldPredicate) throws Exception {
        TestContainers testContainers = getTestContainers(context);

        createAndStartTestContainers(context, testContainers);

        injectFields(context.getTestInstance().orElse(null), context.getRequiredTestClass(), testContainers, fieldPredicate);
    }

    private static void createAndStartTestContainers(ExtensionContext context, TestContainers testContainers) {
        List<TestContainerFactory> factories = context.getElement()
            .map(e -> MergedAnnotations.from(e)
                .stream(WithTestContainerSupport.class)
                .map(TestContainersExtension::createContainerFactory)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        factories.forEach(factory -> {
            String containerName = factory.getContainerName();
            if (!testContainers.contains(containerName)) {
                GenericContainer<?> container = factory.createContainer();
                container.start();
                testContainers.put(containerName, container);
            }
        });
    }

    private static String getContainerName(ParameterContext parameterContext) {
        return parameterContext.findAnnotation(Container.class)
            .map(Container::value)
            .orElse(parameterContext.getParameter().getName());
    }

    private static void injectFields(
        Object instance, Class<?> testClass,
        TestContainers testContainers,
        Predicate<Field> fieldPredicate
    ) throws Exception {
        for (Field field : findAnnotatedFields(testClass, Container.class, fieldPredicate, TOP_DOWN)) {
            injectField(instance, testContainers, field);
        }
    }

    private static void injectField(Object instance, TestContainers testContainers, Field field) throws Exception {
        String containerName = field.getAnnotation(Container.class).value();
        if (!testContainers.contains(containerName)) {
            throw new JUnitException(String.format("Failed to set field %s cause container not found: %s", field, containerName));
        }

        GenericContainer<?> container = testContainers.get(containerName);
        if (!field.getType().isAssignableFrom(container.getClass())) {
            throw new JUnitException(String.format("Failed to insert container of type %s into field %s",
                container.getClass().getSimpleName(), field));
        }

        makeAccessible(field).set(instance, container);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TestContainerFactory createContainerFactory(MergedAnnotation<WithTestContainerSupport> supportAnnotation) {
        Class<? extends TestContainerFactory> factoryClass = (Class<? extends TestContainerFactory>) supportAnnotation.getClass("value");
        TestContainerFactory factory = ReflectionSupport.newInstance(factoryClass);

        if (factory instanceof AnnotationConsumer) {
            // meta source is never null because @WithTestContainerSupport has only Annotation Type level
            MergedAnnotation<?> metaSource = requireNonNull(supportAnnotation.getMetaSource());

            // expected source annotation is never null because AnnotationConsumer has exact 1 generic type
            Class<?> expectedSourceAnnotation = requireNonNull(GenericTypeResolver.resolveTypeArgument(factoryClass, AnnotationConsumer.class));

            Annotation sourceAnnotation = metaSource.synthesize();
            if (!Objects.equals(expectedSourceAnnotation, sourceAnnotation.annotationType())) {
                throw new JUnitException(String.format("%s implementing %s<%s> but is a meta annotation for @%s",
                    factoryClass.getSimpleName(), AnnotationConsumer.class.getSimpleName(),
                    expectedSourceAnnotation.getSimpleName(), sourceAnnotation.annotationType().getSimpleName()));
            }

            ((AnnotationConsumer) factory).accept(sourceAnnotation);
        }

        return factory;
    }
}
