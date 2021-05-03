package io.github.ulisse1996.jaorm.entity.converter;

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
    void should_convert_string_using_converter(String before, ParameterConverter converter, Object after, Class<?> klass) {
        Assertions.assertEquals(after, converter.toValue(before));
        Assertions.assertEquals(klass, converter.getKlass());
    }

    public static Stream<Arguments> getValues() {
        return Stream.of(
                Arguments.arguments("S", ParameterConverter.NONE, "S", String.class),
                Arguments.arguments("1", ParameterConverter.LONG, 1L, Long.class),
                Arguments.arguments("true", ParameterConverter.BOOLEAN, true, Boolean.class),
                Arguments.arguments("1", ParameterConverter.BIG_DECIMAL, BigDecimal.ONE, BigDecimal.class),
                Arguments.arguments("1", ParameterConverter.BIG_INTEGER, BigInteger.ONE, BigInteger.class),
                Arguments.arguments("1", ParameterConverter.INTEGER, 1, Integer.class),
                Arguments.arguments("1.5", ParameterConverter.FLOAT, 1.5f, Float.class),
                Arguments.arguments("1.5", ParameterConverter.DOUBLE, 1.5d, Double.class)
        );
    }
}
