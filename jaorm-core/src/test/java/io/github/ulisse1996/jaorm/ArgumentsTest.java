package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class ArgumentsTest {

    @Test
    void should_return_empty_array() {
        Assertions.assertTrue(Arrays.equals(new Object[0], Arguments.empty().getValues()));
    }

    @Test
    void should_return_empty_array_with_empty_varargs() {
        Assertions.assertTrue(Arrays.equals(new Object[0], Arguments.of().getValues()));
    }

    @Test
    void should_return_same_values() {
        Assertions.assertTrue(Arrays.equals(new Object[] {1, 2}, Arguments.of(1,2).getValues()));
    }

    @Test
    void should_return_same_array() {
        Object[] expected = {1, 2};
        Assertions.assertTrue(Arrays.equals(expected, Arguments.values(expected).getValues()));
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableAssertion"})
    @Test
    void should_not_return_equals_arguments() {
        Arguments expected = Arguments.of(1, 2, 3);
        Arguments result = null;
        Assertions.assertFalse(expected.equals(result)); //NOSONAR
    }

    @Test
    void should_transform_arguments_to_sql_parameters() throws SQLException {
        Arguments arguments = Arguments.of(1, 2, 3);
        List<SqlParameter> parameterList = arguments.asSqlParameters();
        Assertions.assertEquals(3, parameterList.size());
        PreparedStatement mock = Mockito.mock(PreparedStatement.class);
        parameterList.forEach(sqlParameter -> {
            try {
                sqlParameter.getAccessor().set(mock, 1, 1);
            } catch (SQLException ex) {
                Assertions.fail(ex);
            }
        });
        Mockito.verify(mock, Mockito.times(3))
                .setInt(1, 1);
    }
}
