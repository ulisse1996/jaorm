package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.ConverterService;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ExtendWith(MockitoExtension.class)
class SimpleQueryRunnerTest {

    private static final List<SqlParameter> EMPTY_LIST = Collections.emptyList();
    private final SimpleQueryRunner testSubject = new SimpleQueryRunner();

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSetMetaData metaData;
    @Mock private ConverterService converterService;

    @BeforeEach
    public void beforeEach() {
        DatasourceProviderImpl.DATA_SOURCE_THREAD_LOCAL.remove();
        try {
            getProvider().set(Mockito.mock(BeanProvider.class));
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @AfterEach
    void reset() {
        try {
            getProvider().set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Singleton<BeanProvider> getProvider() throws NoSuchFieldException, IllegalAccessException {
        Field field = BeanProvider.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        return (Singleton<BeanProvider>) field.get(null);
    }

    @Test
    void should_return_false_for_not_compatible_class() {
        try (MockedStatic<ConverterService> mk = Mockito.mockStatic(ConverterService.class)) {
            mk.when(ConverterService::getInstance)
                    .thenReturn(converterService);
            Assertions.assertFalse(testSubject.isCompatible(Object.class));
        }
    }

    @Test
    void should_return_true_for_compatible_class() {
        Assertions.assertTrue(testSubject.isCompatible(String.class));
    }

    @Test
    void should_return_true_for_simple_runner() {
        Assertions.assertTrue(testSubject.isSimple());
    }

    @Test
    void should_read_stream_of_simple_object() throws Exception {
        List<String> expected = Arrays.asList("1", "2", "3");
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, true, true, false);
        Mockito.when(resultSet.getString("COL1"))
                .thenReturn("1", "2", "3");
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("COL1");
        List<String> result = testSubject.readStream(String.class, "", Collections.emptyList())
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
        assertClosed(connection, preparedStatement, resultSet);
    }

    @Test
    void should_read_cursor_of_simple_object() throws Exception {
        List<String> expected = Arrays.asList("1", "2", "3");
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, true, true, false);
        Mockito.when(resultSet.getString("COL1"))
                .thenReturn("1", "2", "3");
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("COL1");
        try (Cursor<String> cursor = testSubject.readCursor(String.class, "", Collections.emptyList())) {
            List<String> result = StreamSupport.stream(cursor.spliterator(), false)
                    .collect(Collectors.toList());
            Assertions.assertEquals(expected, result);
        }
    }

    @SuppressWarnings("resource")
    @Test
    void should_read_table_row() throws Exception {
        String expected = "EXPECTED";
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true);
        Mockito.when(resultSet.getString("COL1"))
                .thenReturn(expected);
        String result = testSubject.read("", Collections.emptyList())
                .mapRow(rs -> rs.getString("COL1"));
        Assertions.assertEquals(expected, result);
        assertClosed(connection, preparedStatement, resultSet);
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_not_found_result_for_read_opt_with_table_row() throws Exception {
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE), Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);
        String result = testSubject.readOpt("", Collections.emptyList())
                .map(row -> {
                    try {
                        return row.mapRow(rs -> rs.getString("COL1"));
                    } catch (SQLException ex) {
                        Assertions.fail(ex);
                    }

                    return null;
                }).orElse("NOT_FOUND");
        Assertions.assertEquals("NOT_FOUND", result);
        assertClosed(connection, preparedStatement, resultSet);
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_found_result_for_read_opt_with_table_row() throws Exception {
        String expected = "EXPECTED";
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE), Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, true);
        Mockito.when(resultSet.getString("COL1"))
                .thenReturn(expected);
        String result = testSubject.readOpt("", Collections.emptyList())
                .map(row -> {
                    try {
                        return row.mapRow(rs -> rs.getString("COL1"));
                    } catch (SQLException ex) {
                        Assertions.fail(ex);
                    }

                    return null;
                }).orElse("NOT_FOUND");
        Assertions.assertEquals(expected, result);
        Mockito.verify(resultSet, Mockito.times(1))
                .previous();
        assertClosed(connection, preparedStatement, resultSet);
    }

    @Test
    void should_return_empty_stream_for_read_stream_with_table_row() throws Exception {
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);
        List<TableRow> rows = testSubject.readStream("", Collections.emptyList())
                .collect(Collectors.toList());
        Assertions.assertEquals(0, rows.size());
        assertClosed(connection, preparedStatement, resultSet);
    }

    @Test
    void should_throw_sql_exception_for_readStream() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.readStream(Object.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @SuppressWarnings("resource")
    @Test
    void should_throw_sql_exception_for_read_with_table_row() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.read("", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_throw_sql_exception_for_read_opt_with_table_row() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE), Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
                    .thenThrow(SQLException.class);
            testSubject.readOpt( "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_readStream_with_table_row() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.readStream( "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_read() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.read(Object.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_read_opt() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.readOpt(Object.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_read_all() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.readAll(Object.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_unsupported_exception_for_insert() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.insert(1, "", EMPTY_LIST));
    }

    @Test
    void should_throw_unsupported_exception_for_insert_batch() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.insertWithBatch(Integer.class, "", EMPTY_LIST));
    }

    @Test
    void should_throw_unsupported_exception_for_update_batch() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.updateWithBatch(Integer.class, "", EMPTY_LIST));
    }

    @Test
    void should_throw_sql_exception_for_update() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.update("", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_delete() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenThrow(SQLException.class);
            testSubject.delete("", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_read_simple_string() {
        try {
            String expected = "OK";
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.getString("COL"))
                    .thenReturn(expected);
            Mockito.when(resultSet.getMetaData())
                    .thenReturn(metaData);
            Mockito.when(metaData.getColumnName(1))
                    .thenReturn("COL");

            SqlParameter parameter = new SqlParameter("PARAM", (pr, index, val) -> pr.setString(index, (String) val));
            String result = testSubject.read(String.class, "SELECT RESULT FROM TABLE WHERE ONE = ?", Collections.singletonList(parameter));

            Mockito.verify(preparedStatement).setString(1, "PARAM");
            Assertions.assertEquals(expected, result);
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_empty_optional_for_read_opt() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);

            Result<String> result = testSubject.readOpt(String.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isPresent());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_optional_with_value_for_read_opt() {
        try {
            String expected = "OK";
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true);
            Mockito.when(resultSet.getString("COL"))
                    .thenReturn(expected);
            Mockito.when(resultSet.getMetaData())
                    .thenReturn(metaData);
            Mockito.when(metaData.getColumnName(1))
                    .thenReturn("COL");

            Result<String> result = testSubject.readOpt(String.class, "", Collections.emptyList());
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(expected, result.get());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_list_for_read_all() {
        try {
            String expected = "OK";
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true)
                    .thenReturn(false);
            Mockito.when(resultSet.getString("COL"))
                    .thenReturn(expected);
            Mockito.when(resultSet.getMetaData())
                    .thenReturn(metaData);
            Mockito.when(metaData.getColumnName(1))
                    .thenReturn("COL");

            List<String> result = testSubject.readAll(String.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(expected, result.get(0));
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_do_update() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            testSubject.update("", Collections.emptyList());
            Mockito.verify(preparedStatement).executeUpdate();
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_do_delete() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            testSubject.delete("", Collections.emptyList());
            Mockito.verify(preparedStatement).executeUpdate();
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    private void assertClosed(AutoCloseable... autoCloseables) throws Exception {
        for (AutoCloseable autoCloseable : autoCloseables) {
            Mockito.verify(autoCloseable, Mockito.times(1))
                    .close();
        }
    }
}
