package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

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

class SubstringFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @ParameterizedTest
    @MethodSource("getTests")
    void should_create_substring_function(Selectable<String> selectable, String expected) {
        Assertions.assertEquals(
                expected,
                SubstringFunction.substring(selectable, 3).apply("MY_TABLE")
        );
    }

    @Test
    void should_use_custom_length() {
        Assertions.assertEquals(
                "SUBSTRING(COL_1, 1, 10)",
                SubstringFunction.substring(COL_1, 1, 10).apply(null)
        );
    }

    @Test
    void should_return_true_for_string_function() {
        Assertions.assertTrue(SubstringFunction.substring(COL_1, 1).isString());
    }

    @Test
    void should_get_empty_params_for_vendor_function() {
        Assertions.assertEquals(
                Collections.emptyList(),
                SubstringFunction.substring(AnsiFunctions.upper(COL_1), 1).getParams()
        );
    }

    @Test
    void should_get_empty_params_for_column() {
        Assertions.assertEquals(
                Collections.emptyList(),
                SubstringFunction.substring(COL_1, 1).getParams()
        );
    }

    @Test
    void should_return_param_for_inline() {
        Assertions.assertEquals(
                Collections.singletonList("EL"),
                SubstringFunction.substring(InlineValue.inline("EL"), 1).getParams()
        );
    }

    private static Stream<Arguments> getTests() {
        return Stream.of(
                Arguments.of(COL_1, "SUBSTRING(MY_TABLE.COL_1, 3, 2147483647)"),
                Arguments.of(InlineValue.inline("EL"), "SUBSTRING(?, 3, 2147483647)"),
                Arguments.of(AnsiFunctions.upper(COL_1), "SUBSTRING(UPPER(MY_TABLE.COL_1), 3, 2147483647)")
        );
    }
}