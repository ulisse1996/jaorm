package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.stream.Stream;

class AnsiFunctionsTest {

    private static final SqlColumn<Object, BigDecimal> COLUMN = SqlColumn.simple("NAME", BigDecimal.class);

    @Test
    void should_throw_unsupported_exception_for_new_instance() {
        Constructor<?> declaredConstructor = AnsiFunctions.class.getDeclaredConstructors()[0];
        declaredConstructor.setAccessible(true);
        try {
            declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
            return;
        }

        Assertions.fail("Should throw unsupported exception !");
    }

    @ParameterizedTest
    @MethodSource("getAggregateSelectables")
    void should_return_min(Selectable<?> selectable, String expected) {
        String realExpected = expected.replace("FN", "MIN");
        Assertions.assertEquals(
                realExpected,
                AnsiFunctions.min(selectable).apply(null)
        );
    }

    @ParameterizedTest
    @MethodSource("getAggregateSelectables")
    void should_return_max(Selectable<?> selectable, String expected) {
        String realExpected = expected.replace("FN", "MAX");
        Assertions.assertEquals(
                realExpected,
                AnsiFunctions.max(selectable).apply(null)
        );
    }

    @ParameterizedTest
    @MethodSource("getAggregateSelectables")
    void should_return_count(Selectable<?> selectable, String expected) {
        String realExpected = expected.replace("FN", "COUNT");
        Assertions.assertEquals(
                realExpected,
                AnsiFunctions.count(selectable).apply(null)
        );
    }

    @ParameterizedTest
    @MethodSource("getAggregateSelectables")
    void should_return_avg(Selectable<?> selectable, String expected) {
        String realExpected = expected.replace("FN", "AVG");
        Assertions.assertEquals(
                realExpected,
                AnsiFunctions.avg(selectable).apply(null)
        );
    }

    @ParameterizedTest
    @MethodSource("getAggregateSelectables")
    void should_return_sum(Selectable<?> selectable, String expected) {
        String realExpected = expected.replace("FN", "SUM");
        Assertions.assertEquals(
                realExpected,
                AnsiFunctions.sum(selectable).apply(null)
        );
    }

    @Test
    void should_return_count_star() {
        Assertions.assertEquals("COUNT(*)", AnsiFunctions.count().apply(null));
    }

    @Test
    void should_return_false_for_string_with_aggregate() {
        Assertions.assertFalse(
                AnsiFunctions.max(InlineValue.inline(1)).isString()
        );
    }

    private static Stream<Arguments> getAggregateSelectables() {
        return Stream.of(
                Arguments.of(InlineValue.inline(3), "FN(?)"),
                Arguments.of(COLUMN, "FN(NAME)"),
                Arguments.of(new CastFn(), "FN(CAST(NAME AS INT))")
        );
    }

    private static class CastFn implements VendorFunction<Object> {

        @Override
        public String apply(String alias) {
            return "CAST(NAME AS INT)";
        }

        @Override
        public boolean isString() {
            return false;
        }
    }
}
