package io.github.ulisse1996.jaorm.vendor.mysql.functions;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

class TrimFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_leading_trim(char c) {
        TrimFunction fn;
        if (c != 143) {
            fn = TrimFunction.trim(TrimType.LEADING, ' ', COL_1);
        } else {
            fn = TrimFunction.trim(TrimType.LEADING, COL_1);
        }
        Assertions.assertEquals(
                "TRIM(LEADING ' ' FROM MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_both_trim(char c) {
        TrimFunction fn;
        if (c != 143) {
            fn = TrimFunction.trim(TrimType.BOTH, ' ', COL_1);
        } else {
            fn = TrimFunction.trim(TrimType.BOTH, COL_1);
        }
        Assertions.assertEquals(
                "TRIM(BOTH ' ' FROM MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_trailing_trim(char c) {
        TrimFunction fn;
        if (c != 143) {
            fn = TrimFunction.trim(TrimType.TRAILING, ' ', COL_1);
        } else {
            fn = TrimFunction.trim(TrimType.TRAILING, COL_1);
        }
        Assertions.assertEquals(
                "TRIM(TRAILING ' ' FROM MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @ParameterizedTest
    @EnumSource(TrimType.class)
    void should_create_trim_function_with_inner_function(TrimType type) {
        TrimFunction fn = TrimFunction.trim(type, AnsiFunctions.upper(COL_1));
        Assertions.assertEquals(
                String.format("TRIM(%s ' ' FROM UPPER(MY_TABLE.COL_1))", type.name()),
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_create_trim_function_with_custom_char_and_inner_function() {
        TrimFunction fn = TrimFunction.trim('1', AnsiFunctions.upper(COL_1));
        Assertions.assertEquals(
                "TRIM( '1' FROM UPPER(MY_TABLE.COL_1))",
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_create_trim_function_with_custom_char_and_column() {
        TrimFunction fn = TrimFunction.trim('1', COL_1);
        Assertions.assertEquals(
                "TRIM( '1' FROM MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_create_default_trim_with_column() {
        TrimFunction fn = TrimFunction.trim(COL_1);
        Assertions.assertEquals(
                "TRIM( ' ' FROM MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_create_default_trim_with_inner_function() {
        TrimFunction fn = TrimFunction.trim(AnsiFunctions.upper(COL_1));
        Assertions.assertEquals(
                "TRIM( ' ' FROM UPPER(MY_TABLE.COL_1))",
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_return_params_for_trim_with_inline() {
        Assertions.assertEquals(
                Collections.singletonList(" EL "),
                TrimFunction.trim(InlineValue.inline(" EL ")).getParams()
        );
    }

    @Test
    void should_return_true_for_string_function() {
        Assertions.assertTrue(TrimFunction.trim(COL_1).isString());
    }
}