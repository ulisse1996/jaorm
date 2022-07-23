package io.github.ulisse1996.jaorm.vendor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ServerVersionTest {

    @ParameterizedTest
    @MethodSource("getVersions")
    void should_get_parsed_version(String s, int major, int minor, int patch) {
        ServerVersion version = ServerVersion.fromString(s);
        Assertions.assertEquals(major, version.getMajor());
        Assertions.assertEquals(minor, version.getMinor());
        Assertions.assertEquals(patch, version.getPatch());
    }

    private static Stream<Arguments> getVersions() {
        return Stream.of(
                Arguments.of("1.10.9", 1, 10, 9),
                Arguments.of("1.10", 1, 10, 0),
                Arguments.of("1", 1, 0, 0),
                Arguments.of("1.0.0 Other", 1, 0, 0)
        );
    }
}
