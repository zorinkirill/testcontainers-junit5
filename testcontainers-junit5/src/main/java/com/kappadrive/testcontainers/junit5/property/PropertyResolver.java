package com.kappadrive.testcontainers.junit5.property;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Specifies property patterns which can be mapped from container metadata into property.
 * Will be applied only to containers which implements or extends {@link T}.
 *
 * @param <T> - super type of accepted test containers.
 */
public interface PropertyResolver<T> {

    /**
     * Returns pattern to be replaced in property value.
     * It is highly recommended to start pattern with <code>${</code> and and with <code>}</code>
     *
     * @return pattern to replaced in property with metadata from container.
     */
    Pattern getPattern();

    /**
     * Returns replace function for each entry found with {@link PropertyResolver#getPattern()}.
     * Supports repeating groups via {@link MatchResult}.
     *
     * @param container - container to get metadata from.
     * @return function to map matched entry into final value.
     */
    Function<MatchResult, String> resolve(T container);
}
