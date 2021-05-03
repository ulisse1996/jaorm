package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
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
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class ResultSetStreamTest {

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @Test
    void should_return_empty_stream() throws SQLException {
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);
        ResultSetExecutor executor = Mockito.spy(getExecutor());
        List<Object> objectList = new ResultSetStream<>(connection, preparedStatement, executor, rs -> new Object())
                .getStream()
                .collect(Collectors.toList());
        Assertions.assertEquals(0, objectList.size());
        Mockito.verify(executor, Mockito.times(1)).close();
        Mockito.verify(preparedStatement, Mockito.times(1)).close();
        Mockito.verify(connection, Mockito.times(1)).close();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void should_throw_jaorm_exception_during_advance() throws SQLException {
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenThrow(SQLException.class);
        ResultSetExecutor executor = Mockito.spy(getExecutor());
        Assertions.assertThrows(JaormSqlException.class, () -> new ResultSetStream<>(connection, preparedStatement, executor, rs -> new Object()) //NOSONAR
                .getStream()
                .collect(Collectors.toList()));
    }

    @Test
    void should_return_collected_stream() throws SQLException {
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, true, true, false);
        ResultSetExecutor executor = Mockito.spy(getExecutor());
        List<Object> objectList = new ResultSetStream<>(connection, preparedStatement, executor, rs -> new Object())
                .getStream()
                .collect(Collectors.toList());
        Assertions.assertEquals(3, objectList.size());
        Mockito.verify(executor, Mockito.times(1)).close();
        Mockito.verify(preparedStatement, Mockito.times(1)).close();
        Mockito.verify(connection, Mockito.times(1)).close();
    }

    private ResultSetExecutor getExecutor() throws SQLException {
        return new ResultSetExecutor(preparedStatement, Collections.emptyList());
    }
}
