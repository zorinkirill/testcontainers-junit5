package com.kappadrive.testcontainers.junit5.property;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract class PropertyResolverUtil {

    static <T> String replace(String input, PropertyResolver<? super T> resolver, T obj) {
        return resolver.getPattern().matcher(input).replaceAll(resolver.resolve(obj));
    }
}
