package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.entity.NullWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SqlParameterTest {

    @Mock private PreparedStatement pr;

    @Test
    void should_return_null_setter() throws SQLException {
        SqlParameter parameter = new SqlParameter(null);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        parameter.getAccessor().set(preparedStatement, 1, null);
        Mockito.verify(preparedStatement).setNull(1, JDBCType.NULL.getVendorTypeNumber());
    }

    @Test
    void should_return_string_accessor_for_null_wrapper() throws SQLException {
        SqlParameter parameter = new SqlParameter(new NullWrapper(String.class));
        parameter.getAccessor().set(pr, 1, "STRING");
        Mockito.verify(pr)
                .setString(1, "STRING");
    }

    @Test
    void should_transform_arguments_to_sql_parameters() {
        List<SqlParameter> expected = Arrays.asList(
                new SqlParameter(null),
                new SqlParameter("22")
        );
        List<SqlParameter> result = SqlParameter.argumentsAsParameters(new Object[] {
                null, "22"
        });
        Assertions.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertEquals(expected.get(i).getVal(), result.get(i).getVal());
        }
    }
}
