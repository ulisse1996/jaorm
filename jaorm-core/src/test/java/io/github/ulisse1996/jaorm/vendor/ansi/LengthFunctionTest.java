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

import java.util.Collections;
import java.util.stream.Stream;

class LengthFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @Test
    void should_return_false_for_string_function() {
        Assertions.assertFalse(AnsiFunctions.length(COL_1).isString());
    }

    @ParameterizedTest
    @MethodSource("getTests")
    void should_create_length(Selectable<String> selectable, String expected) {
        Assertions.assertEquals(
                expected,
                AnsiFunctions.length(selectable).apply("MY_TABLE")
        );
    }

    @Test
    void should_get_empty_params_for_column() {
        Assertions.assertEquals(
                Collections.emptyList(),
                AnsiFunctions.length(COL_1).getParams()
        );
    }

    @Test
    void should_get_param_from_inline() {
        Assertions.assertEquals(
                Collections.singletonList("323"),
                AnsiFunctions.length(InlineValue.inline("323")).getParams()
        );
    }

    private static Stream<Arguments> getTests() {
        return Stream.of(
                Arguments.of(COL_1, "LENGTH(MY_TABLE.COL_1)"),
                Arguments.of(AnsiFunctions.upper(COL_1), "LENGTH(UPPER(MY_TABLE.COL_1))"),
                Arguments.of(InlineValue.inline("31234"), "LENGTH(?)")
        );
    }
}