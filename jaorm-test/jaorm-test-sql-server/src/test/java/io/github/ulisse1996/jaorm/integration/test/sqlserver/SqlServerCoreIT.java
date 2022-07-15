package io.github.ulisse1996.jaorm.integration.test.sqlserver;

import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.integration.test.CoreIT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlServerCoreIT extends CoreIT {

    @Override
    @Test
    public void should_insert_with_auto_generated() {
        // SQL Server JDBC driver doesn't support return of ids from batch
        Assertions.assertThrows(JaormSqlException.class, super::should_insert_with_auto_generated);
    }

    @Override
    @Test
    public void should_return_all_generated_columns() {
        // SQL Server JDBC driver only support 1 parameter generation
        Assertions.assertThrows(UnsupportedOperationException.class, super::should_return_all_generated_columns);
    }
}
