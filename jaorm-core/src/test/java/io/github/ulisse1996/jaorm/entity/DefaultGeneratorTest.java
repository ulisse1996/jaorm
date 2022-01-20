package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;

class DefaultGeneratorTest {

    @Test
    void should_return_big_decimal() {
        BigDecimal result = DefaultGenerator.forNumeric(BigDecimal.class, 1.5);
        Assertions.assertEquals(0, BigDecimal.valueOf(1.5).compareTo(result));
    }

    @Test
    void should_return_big_integer() {
        BigInteger result = DefaultGenerator.forNumeric(BigInteger.class, 1.5);
        Assertions.assertEquals(0, BigInteger.valueOf((long) 1.5).compareTo(result));
    }

    @Test
    void should_throw_exception_for_unexpected_type() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> DefaultGenerator.forNumeric(Object.class, 0));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Date.class,
            java.sql.Date.class,
            Time.class,
            Timestamp.class,
            Instant.class,
            OffsetDateTime.class,
            ZonedDateTime.class,
            LocalDate.class,
            LocalDateTime.class,
            LocalDateTime.class,
            LocalTime.class,
            OffsetTime.class
    })
    void should_generate_default_temporal(Class<?> klass) {
        Assertions.assertTrue(klass.isInstance(DefaultGenerator.forTemporal(klass)));
    }

    @Test
    void should_throw_exception_for_unexpected_temporal() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> DefaultGenerator.forTemporal(Object.class));
    }

    @Test
    void should_throw_exception_for_unexpected_temporal_with_format() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> DefaultGenerator.forTemporal(Object.class, "ddMMyyyy", "01012022"));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Date.class,
            java.sql.Date.class,
            Time.class,
            Timestamp.class,
            Instant.class,
            LocalDate.class,
            LocalDateTime.class,
            LocalDateTime.class,
            LocalTime.class,
    })
    void should_convert_temporal_with_format(Class<?> klass) {
        String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String value = "2013-09-29T18:46:19Z";
        Assertions.assertTrue(klass.isInstance(DefaultGenerator.forTemporal(klass, format, value)));
    }

    @Test
    void should_return_same_value_with_coercion() {
        Assertions.assertAll(
                () -> Assertions.assertEquals((byte) 0, DefaultGenerator.forNumeric(byte.class, 0)),
                () -> Assertions.assertEquals((byte) 0, DefaultGenerator.forNumeric(Byte.class, 0)),

                () -> Assertions.assertEquals((short) 0, DefaultGenerator.forNumeric(short.class, 0)),
                () -> Assertions.assertEquals((short) 0, DefaultGenerator.forNumeric(Short.class, 0)),

                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(int.class, 0)),
                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(Integer.class, 0)),

                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(long.class, 0)),
                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(Long.class, 0)),

                () -> Assertions.assertEquals((float) 0, DefaultGenerator.forNumeric(float.class, 0)),
                () -> Assertions.assertEquals((float) 0, DefaultGenerator.forNumeric(Float.class, 0)),

                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(double.class, 0)),
                () -> Assertions.assertEquals(0, DefaultGenerator.forNumeric(Double.class, 0))
        );
    }
}
