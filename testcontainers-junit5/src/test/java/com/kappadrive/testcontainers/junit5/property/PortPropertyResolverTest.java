package com.kappadrive.testcontainers.junit5.property;

import static com.kappadrive.testcontainers.junit5.property.PropertyResolverUtil.replace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(MockitoExtension.class)
class PortPropertyResolverTest {

    @Mock
    private GenericContainer<?> container;

    @CsvSource({
        "666,777,${port:666},777",
        "00001,777,${port:1},777",
        "99999,777,${port:99999},777",
    })
    @ParameterizedTest
    void testResolvePort(int originalPort, int mappedPort, String input, String expected) {
        given(container.getMappedPort(originalPort)).willReturn(mappedPort);

        String result = replace(input, new PortPropertyResolver(), container);

        assertThat(result).isEqualTo(expected);
    }

    @CsvSource({
        "${port}",
        "${port:-1}",
        "${port:999999}",
        "${not-a-port}",
    })
    @ParameterizedTest
    void testUnresolvablePort(String input) {
        String result = replace(input, new PortPropertyResolver(), container);

        assertThat(result).isEqualTo(input);
    }

}
