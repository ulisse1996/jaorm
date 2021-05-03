package io.github.ulisse1996.jaorm.entity.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BooleanIntConverterTest {

    @ParameterizedTest
    @MethodSource("intsToBooleans")
    void should_convert_integers_to_booleans(Integer input, Boolean expected) {
        Assertions.assertEquals(expected, BooleanIntConverter.INSTANCE.fromSql(input));
    }

    @ParameterizedTest
    @MethodSource("booleansToInts")
    void should_convert_booleans_to_integers(Boolean input, Integer expected) {
        Assertions.assertEquals(expected, BooleanIntConverter.INSTANCE.toSql(input));
    }

    public static Stream<Arguments> booleansToInts() {
        return Stream.of(
                Arguments.arguments(null, 0),
                Arguments.arguments(true, 1),
                Arguments.arguments(false, 0)
        );
    }

    public static Stream<Arguments> intsToBooleans() {
        return Stream.of(
                Arguments.arguments(null, false),
                Arguments.arguments(1, true),
                Arguments.arguments(0, false)
        );
    }
}
