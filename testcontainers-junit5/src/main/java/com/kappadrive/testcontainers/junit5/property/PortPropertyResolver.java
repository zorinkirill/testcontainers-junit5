package com.kappadrive.testcontainers.junit5.property;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.testcontainers.containers.ContainerState;

/**
 * Resolver for exposed by container port.
 */
public class PortPropertyResolver implements PropertyResolver<ContainerState> {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{port:([0-9]{1,5})}");

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public Function<MatchResult, String> resolve(ContainerState container) {
        return res -> container.getMappedPort(Integer.parseInt(res.group(1))).toString();
    }
}
