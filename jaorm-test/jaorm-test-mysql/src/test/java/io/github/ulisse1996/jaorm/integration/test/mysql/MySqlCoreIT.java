package io.github.ulisse1996.jaorm.integration.test.mysql;

import io.github.ulisse1996.jaorm.integration.test.CoreIT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MySqlCoreIT extends CoreIT {

    @Override
    @Test
    public void should_return_all_generated_columns() {
        // MySQL JDBC driver only support 1 parameter generation
        Assertions.assertThrows(UnsupportedOperationException.class, super::should_return_all_generated_columns);
    }
}
