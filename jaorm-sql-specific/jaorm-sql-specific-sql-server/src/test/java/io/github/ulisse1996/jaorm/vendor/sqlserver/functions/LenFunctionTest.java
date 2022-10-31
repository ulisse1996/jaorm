package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LenFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @Test
    void should_return_true_for_string_fn() {
        Assertions.assertTrue(LenFunction.len(COL_1).isString());
    }

    @Test
    void should_return_len_fn() {
        Assertions.assertEquals(
                "LEN(COL_1)",
                LenFunction.len(COL_1).apply(null)
        );
    }
}