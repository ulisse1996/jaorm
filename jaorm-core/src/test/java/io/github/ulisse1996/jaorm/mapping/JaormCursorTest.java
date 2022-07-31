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
import java.util.Iterator;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class JaormCursorTest {

    private static final ThrowingFunction<ResultSet, Object, SQLException> FN = resultSet -> new Object();
    private static final ThrowingFunction<ResultSet, Object, SQLException> FN_THROWING = rs -> {
        throw new SQLException();
    };

    @Mock private Connection connection;
    @Mock private PreparedStatement pr;
    @Mock private ResultSetExecutor executor;
    @Mock private ResultSet resultSet;

    @Test
    void should_return_true_for_fetched_after_iterator_get() {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN
        );

        Assertions.assertFalse(cursor.isFetched());
        cursor.iterator();
        Assertions.assertTrue(cursor.isFetched());
    }

    @Test
    void should_throw_exception_for_duplicate_cursor_get() {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN
        );

        cursor.iterator();
        Assertions.assertThrows(
                IllegalStateException.class,
                cursor::iterator
        );
    }

    @Test
    void should_throw_exception_for_iterator_get_after_close() {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN
        );

        cursor.iterator();
        cursor.close();
        Assertions.assertThrows(
                IllegalStateException.class,
                cursor::iterator
        );
    }

    @Test
    void should_throw_exception_for_missing_next() {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN
        );
        Assertions.assertThrows( //NOSONAR
                NoSuchElementException.class,
                () -> cursor.iterator().next()
        );
    }

    @Test
    void should_throw_exception_during_iterator_creation() throws SQLException {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenThrow(SQLException.class);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN
        );
        Assertions.assertThrows(
                JaormSqlException.class,
                cursor::iterator
        );
    }

    @Test
    void should_throw_exception_during_read() throws SQLException {
        Mockito.when(executor.getResultSet()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true, false);
        JaormCursor<Object> cursor = new JaormCursor<>(
                connection,
                pr,
                executor,
                FN_THROWING
        );
        Iterator<Object> iterator = cursor.iterator();
        Assertions.assertThrows(
                JaormSqlException.class,
                iterator::next
        );
    }
}
