package io.jaorm.entity.sql;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class SqlParameterTest {

    @Test
    void should_return_null_setter() throws SQLException {
        SqlParameter parameter = new SqlParameter(null);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        parameter.getAccessor().set(preparedStatement, 1, null);
        Mockito.verify(preparedStatement).setNull(1, JDBCType.NULL.getVendorTypeNumber());
    }
}