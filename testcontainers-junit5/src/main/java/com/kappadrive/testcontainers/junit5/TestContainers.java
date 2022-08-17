package com.kappadrive.testcontainers.junit5;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

/**
 * Container for Test Containers available in Jupiter test context.
 * Could be accessed via:
 * <pre>{@code
 * TestContainers.getTestContainers(context);
 * }</pre>
 */
@Log4j2
public class TestContainers implements ExtensionContext.Store.CloseableResource {

    private static final String CONTAINERS_KEY = "containers";

    private final Map<String, GenericContainer<?>> containerMap = new HashMap<>();

    /**
     * Returns <code>TestContainers</code> from Jupiter test context.
     * It is expected that will be no more than 1 instance of test containers in context.
     *
     * @param context - test context to search.
     * @return test containers from test context.
     */
    public static TestContainers getTestContainers(ExtensionContext context) {
        return context.getRoot().getStore(GLOBAL)
            .getOrComputeIfAbsent(CONTAINERS_KEY, k -> new TestContainers(), TestContainers.class);
    }

    @Override
    public void close() {
        containerMap.forEach(TestContainers::stopSafe);
    }

    /**
     * Returns if there is container with the <code>containerName</code> in context.
     *
     * @param containerName - container name to search.
     * @return <code>true</code> if there container for given name, <code>false</code> otherwise.
     */
    public boolean contains(String containerName) {
        return containerMap.containsKey(containerName);
    }

    /**
     * Returns container from context with name <code>containerName</code>.
     *
     * @param containerName - container name to search.
     * @return container from context by given name, possible <code>null</code>.
     */
    public GenericContainer<?> get(String containerName) {
        return containerMap.get(containerName);
    }

    /**
     * Adds container to context.
     *
     * @param containerName - name of container to add.
     * @param container     - container to add.
     */
    public void put(String containerName, GenericContainer<?> container) {
        containerMap.put(containerName, container);
    }

    private static void stopSafe(String containerName, GenericContainer<?> container) {
        try {
            container.stop();
        } catch (Throwable e) {
            log.error("Failed to stop container {}", containerName, e);
        }
    }
}
