package com.kappadrive.testcontainers.junit5.property;

import static com.kappadrive.testcontainers.junit5.property.PropertyResolverUtil.replace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(MockitoExtension.class)
class HostPropertyResolverTest {

    @Mock
    private GenericContainer<?> container;

    @Test
    void testResolveHost() {
        given(container.getHost()).willReturn("test-host");

        String result = replace("${host}", new HostPropertyResolver(), container);

        assertThat(result).isEqualTo("test-host");
    }

}