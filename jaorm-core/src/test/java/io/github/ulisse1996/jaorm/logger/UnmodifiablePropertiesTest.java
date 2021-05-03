package io.github.ulisse1996.jaorm.logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.stream.Stream;

class UnmodifiablePropertiesTest {

    @ParameterizedTest
    @MethodSource("getTests")
    void should_throw_exception_for_new_values(Executable executable) {
        Assertions.assertThrows(UnsupportedOperationException.class, executable);
    }

    public static Stream<Arguments> getTests() {
        final UnmodifiableProperties testSubject = new UnmodifiableProperties(new Properties());
        return Stream.of(
                Arguments.of((Executable)() -> testSubject.put("", "")),
                Arguments.of((Executable)() -> testSubject.putIfAbsent("", "")),
                Arguments.of((Executable)() -> testSubject.load((InputStream) null)),
                Arguments.of((Executable)() -> testSubject.loadFromXML(null)),
                Arguments.of((Executable)() -> testSubject.load((Reader) null)),
                Arguments.of((Executable)() -> testSubject.putAll(null))
        );
    }
}
