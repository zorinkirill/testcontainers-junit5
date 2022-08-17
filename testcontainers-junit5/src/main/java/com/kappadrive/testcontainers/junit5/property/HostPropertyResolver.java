package com.kappadrive.testcontainers.junit5.property;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.testcontainers.containers.ContainerState;

/**
 * Resolver for container host.
 */
public class HostPropertyResolver implements PropertyResolver<ContainerState> {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{host}");

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public Function<MatchResult, String> resolve(ContainerState container) {
        return res -> container.getHost();
    }
}
