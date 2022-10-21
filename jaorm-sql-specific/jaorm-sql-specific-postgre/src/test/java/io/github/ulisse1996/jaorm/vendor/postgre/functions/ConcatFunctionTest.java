package io.github.ulisse1996.jaorm.vendor.postgre.functions;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ConcatFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @Test
    void should_concat_values() {
        ConcatFunction fn = ConcatFunction.concat(AnsiFunctions.upper(COL_1), InlineValue.inline("CUSTOM_VALUE"), COL_1);
        Assertions.assertEquals(
                "CONCAT(UPPER(MY_TABLE.COL_1), ?, MY_TABLE.COL_1)",
                fn.apply("MY_TABLE")
        );
    }

    @Test
    void should_return_true_for_string_function() {
        Assertions.assertTrue(ConcatFunction.concat().isString());
    }

    @Test
    void should_only_return_string_params() {
        ConcatFunction fn = ConcatFunction.concat(COL_1, COL_1, COL_1, InlineValue.inline("1"), COL_1, InlineValue.inline("2"));
        Assertions.assertEquals(
                Arrays.asList("1", "2"),
                fn.getParams()
        );
    }
}