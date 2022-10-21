package io.github.ulisse1996.jaorm.vendor.postgre.functions;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.stream.Stream;

class SubstringFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @ParameterizedTest
    @MethodSource("getTests")
    void should_create_substring(Selectable<String> selectable, String expected) {
        Assertions.assertEquals(
                expected,
                SubstringFunction.substring(selectable, 7).apply("MY_TABLE")
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void should_throw_exception_for_invalid_start(long start) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> SubstringFunction.substring(COL_1, start, 7)
        );
    }

    @Test
    void should_get_empty_params_for_column() {
        Assertions.assertEquals(
                Collections.emptyList(),
                SubstringFunction.substring(COL_1, 7).getParams()
        );
    }

    @Test
    void should_get_empty_params_for_vendor_function() {
        Assertions.assertEquals(
                Collections.emptyList(),
                SubstringFunction.substring(AnsiFunctions.upper(COL_1), 7).getParams()
        );
    }

    @Test
    void should_get_params_for_inline_value() {
        Assertions.assertEquals(
                Collections.singletonList("2"),
                SubstringFunction.substring(InlineValue.inline("2"), 7).getParams()
        );
    }

    @Test
    void should_return_true_for_string_function() {
        Assertions.assertTrue(SubstringFunction.substring(InlineValue.inline("2"), 7).isString());
    }

    @Test
    void should_return_substring_with_custom_start() {
        Assertions.assertEquals(
                "SUBSTRING(?, 10, 15)",
                SubstringFunction.substring(InlineValue.inline("2334"), 10, 15).apply("")
        );
    }

    private static Stream<Arguments> getTests() {
        return Stream.of(
                Arguments.of(COL_1, "SUBSTRING(MY_TABLE.COL_1, 7)"),
                Arguments.of(AnsiFunctions.upper(COL_1), "SUBSTRING(UPPER(MY_TABLE.COL_1), 7)"),
                Arguments.of(InlineValue.inline("12345"), "SUBSTRING(?, 7)")
        );
    }
}