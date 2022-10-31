package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class CoalesceTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);
    private static final SqlColumn<Object, Long> COL_2 = SqlColumn.simple("COL_2", Long.class);

    @Test
    void should_create_coalesce_sql() {
        CoalesceFunction<String> coalesce = AnsiFunctions.coalesce(COL_1, InlineValue.inline("1"), AnsiFunctions.upper(COL_1));
        Assertions.assertEquals(
                "COALESCE(MY_TABLE.COL_1, ?, UPPER(MY_TABLE.COL_1))",
                coalesce.apply("MY_TABLE")
        );
    }

    @ParameterizedTest
    @MethodSource("getSelectables")
    void should_return_true_for_string_column(Selectable<?> selectable) {
        Assertions.assertTrue(
                AnsiFunctions.coalesce(selectable).isString()
        );
    }

    @Test
    void should_return_params_for_coalesce_inlines() {
        Assertions.assertEquals(
                Arrays.asList("3", "4", "5", "EL"),
                AnsiFunctions.coalesce(
                        InlineValue.inline("3"),
                        InlineValue.inline("4"),
                        InlineValue.inline("5"),
                        InlineValue.inline("EL")
                ).getParams()
        );
    }

    private static Stream<Arguments> getSelectables() {
        return Stream.of(
                Arguments.of(COL_1),
                Arguments.of(AnsiFunctions.upper(COL_1)),
                Arguments.of(InlineValue.inline("3"))
        );
    }
}