package io.github.ulisse1996.jaorm.vendor.sqlserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlServerCountSpecificTest {

    @Test
    void should_return_required_count_name() {
        Assertions.assertTrue(new SqlServerCountSpecific().isNamedCountRequired());
    }
}
