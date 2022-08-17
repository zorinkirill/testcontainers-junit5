package com.kappadrive.testcontainers.junit5.extension;

import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockito.Mockito;
import org.testcontainers.DockerClientFactory;

class MockDockerClientFactoryExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Field instance = DockerClientFactory.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, Mockito.mock(DockerClientFactory.class));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Field instance = DockerClientFactory.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return DockerClientFactory.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return DockerClientFactory.instance();
    }
}
