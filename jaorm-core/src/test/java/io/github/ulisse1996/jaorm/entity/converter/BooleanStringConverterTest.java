package io.github.ulisse1996.jaorm.entity.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BooleanStringConverterTest {

    @ParameterizedTest
    @MethodSource("stringToBooleans")
    void should_convert_strings_to_booleans(String input, Boolean expected) {
        Assertions.assertEquals(expected, BooleanStringConverter.INSTANCE.fromSql(input));
    }

    @ParameterizedTest
    @MethodSource("booleansToString")
    void should_convert_booleans_to_strings(Boolean input, String expected) {
        Assertions.assertEquals(expected, BooleanStringConverter.INSTANCE.toSql(input));
    }

    public static Stream<Arguments> stringToBooleans() {
        return Stream.of(
                Arguments.arguments(null, false),
                Arguments.arguments("", false),
                Arguments.arguments("Y", true),
                Arguments.arguments("N", false)
        );
    }

    public static Stream<Arguments> booleansToString() {
        return Stream.of(
                Arguments.arguments(null, "N"),
                Arguments.arguments(true, "Y"),
                Arguments.arguments(false, "N")
        );
    }
}
