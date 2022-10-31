package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.sqlserver.functions.LenFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlServerLengthSpecificTest {

    @Test
    void should_return_len_instance() {
        Assertions.assertTrue(
                new SqlServerLengthSpecific().apply(SqlColumn.simple("COL_1", String.class))
                instanceof LenFunction
        );
    }
}