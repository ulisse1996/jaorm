package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class TableRowTest {

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @Test
    void should_map_row() throws SQLException {
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        ResultSetExecutor executor = Mockito.spy(getExecutor());
        try (TableRow row = new TableRow(connection, preparedStatement, executor)) {
            Object o = row.mapRow(rs -> new Object());
            Assertions.assertNotNull(o);
            Mockito.verify(resultSet, Mockito.times(1)).next();
        }
        Mockito.verify(connection, Mockito.times(2)).close();
        Mockito.verify(executor, Mockito.times(2)).close();
        Mockito.verify(preparedStatement, Mockito.times(2)).close();
    }

    private ResultSetExecutor getExecutor() throws SQLException {
        return new ResultSetExecutor(preparedStatement, Collections.emptyList());
    }

}
