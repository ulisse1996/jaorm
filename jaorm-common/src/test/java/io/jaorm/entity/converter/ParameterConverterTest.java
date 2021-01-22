package io.jaorm.entity.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

class ParameterConverterTest {

    @ParameterizedTest
    @MethodSource("getValues")
    void should_convert_string_using_converter(String before, ParameterConverter converter, Object after) {
        Assertions.assertEquals(after, converter.toValue(before));
    }

    public static Stream<Arguments> getValues() {
        return Stream.of(
                Arguments.arguments("S", ParameterConverter.NONE, "S"),
                Arguments.arguments("1", ParameterConverter.LONG, 1L),
                Arguments.arguments("true", ParameterConverter.BOOLEAN, true),
                Arguments.arguments("1", ParameterConverter.BIG_DECIMAL, BigDecimal.ONE),
                Arguments.arguments("1", ParameterConverter.BIG_INTEGER, BigInteger.ONE),
                Arguments.arguments("1", ParameterConverter.INTEGER, 1),
                Arguments.arguments("1.5", ParameterConverter.FLOAT, 1.5f),
                Arguments.arguments("1.5", ParameterConverter.DOUBLE, 1.5d)
        );
    }
}