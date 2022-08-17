package com.kappadrive.testcontainers.junit5.property;

import static java.util.Objects.requireNonNull;

import com.kappadrive.testcontainers.junit5.TestContainers;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.MergedAnnotations;
import org.testcontainers.containers.GenericContainer;

class MapToSystemPropertyExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        setSystemProperties(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        setSystemProperties(context);
    }

    private static void setSystemProperties(ExtensionContext context) {
        TestContainers testContainers = TestContainers.getTestContainers(context);

        @SuppressWarnings({"unchecked"})
        List<? extends PropertyResolver<?>> resolvers = getAllContexts(context).stream()
            .flatMap(e -> e.getElement().stream())
            .flatMap(e -> MergedAnnotations.from(e).stream(WithPropertyMapper.class))
            .flatMap(a -> Stream.of(a.getClassArray("value")))
            .map(c -> (Class<? extends PropertyResolver<?>>) c)
            .distinct()
            .map(ReflectionUtils::newInstance)
            .collect(Collectors.toList());

        AnnotationSupport.findRepeatableAnnotations(context.getElement(), MapToSystemProperty.class)
            .forEach(mapToSystemProperty -> {
                if (!testContainers.contains(mapToSystemProperty.container())) {
                    throw new JUnitException(String.format("Failed to set property '%s' cause container not found: %s",
                        mapToSystemProperty.property(), mapToSystemProperty.container()));
                }
                GenericContainer<?> container = testContainers.get(mapToSystemProperty.container());
                List<? extends PropertyResolver<?>> supportedResolvers = resolvers.stream()
                    .filter(resolver -> {
                        // never null, because PropertyResolver interface has exact 1 generic type
                        Class<?> expectedContainerClass =
                            requireNonNull(GenericTypeResolver.resolveTypeArgument(resolver.getClass(), PropertyResolver.class));
                        return expectedContainerClass.isAssignableFrom(container.getClass());
                    })
                    .collect(Collectors.toList());
                setProperty(mapToSystemProperty, container, supportedResolvers);
            });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setProperty(MapToSystemProperty mapToSystemProperty, GenericContainer<?> container,
                                    List<? extends PropertyResolver<?>> resolvers) {
        String value = mapToSystemProperty.value();

        for (PropertyResolver resolver : resolvers) {
            Matcher matcher = resolver.getPattern().matcher(value);
            value = matcher.replaceAll(resolver.resolve(container));
        }

        System.setProperty(mapToSystemProperty.property(), value);
    }

    private static List<ExtensionContext> getAllContexts(ExtensionContext context) {
        List<ExtensionContext> contexts = new ArrayList<>();
        contexts.add(context);
        context.getParent().ifPresent(parent -> contexts.addAll(getAllContexts(parent)));
        return contexts;
    }
}
