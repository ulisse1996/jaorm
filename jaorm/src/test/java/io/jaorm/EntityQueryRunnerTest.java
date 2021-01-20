package io.jaorm;

import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EntityQueryRunnerTest {

    private final EntityQueryRunner testSubject = new EntityQueryRunner();

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    public void beforeAll() {
        DatasourceProviderImpl.DATA_SOURCE_THREAD_LOCAL.remove();
    }

    @Test
    void should_return_false_for_simple() {
        Assertions.assertFalse(testSubject.isSimple());
    }

    @Test
    void should_return_true_for_compatible_delegate() {
        Assertions.assertTrue(testSubject.isCompatible(DelegatesImpl.MyEntity.class));
    }

    @Test
    void should_return_false_for_missing_delegate() {
        Assertions.assertFalse(testSubject.isCompatible(Object.class));
    }

    @Test
    void should_throw_sql_exception_for_read() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenThrow(SQLException.class);
            testSubject.read(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
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
                    .thenThrow(SQLException.class);
            testSubject.readOpt(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
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
                    .thenThrow(SQLException.class);
            testSubject.readAll(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_insert() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenThrow(SQLException.class);
            testSubject.insert(new DelegatesImpl.MyEntity(), "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_throw_sql_exception_for_update() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
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
                    .thenThrow(SQLException.class);
            testSubject.delete( "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

    @Test
    void should_return_mapped_entity_for_read() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true);
            setResultSet(expected);
            DelegatesImpl.MyEntity result = testSubject.read(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
            Assertions.assertTrue(result instanceof EntityDelegate);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(expected.getField1(), result.getField1()),
                    () -> Assertions.assertEquals(expected.getField2(), result.getField2())
            );
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_empty_optional_for_read_opt() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(false);
            Optional<DelegatesImpl.MyEntity> result = testSubject.readOpt(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isPresent());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_optional_with_value_for_read_opt() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true);
            setResultSet(expected);
            Optional<DelegatesImpl.MyEntity> result = testSubject.readOpt(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
            Assertions.assertTrue(result.isPresent());
            checkResult(expected, result.get());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_list_for_read_all() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
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
            setResultSet(expected);
            List<DelegatesImpl.MyEntity> result = testSubject.readAll(DelegatesImpl.MyEntity.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(1, result.size());
            checkResult(expected, result.get(0));
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_same_entity_after_insert() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            DelegatesImpl.MyEntity result = testSubject.insert(expected, "QUERY", Collections.emptyList());
            Assertions.assertEquals(expected, result);
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_do_update() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            testSubject.update("", Collections.emptyList());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_do_delete() {
        try {
            DelegatesImpl.MyEntity expected = new DelegatesImpl.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            testSubject.delete("", Collections.emptyList());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    private void checkResult(DelegatesImpl.MyEntity expected, DelegatesImpl.MyEntity result) {
        Assertions.assertTrue(result instanceof EntityDelegate);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expected.getField1(), result.getField1()),
                () -> Assertions.assertEquals(expected.getField2(), result.getField2())
        );
    }

    private void setResultSet(DelegatesImpl.MyEntity expected) throws SQLException {
        Mockito.when(resultSet.getString("FIELD1"))
                .thenReturn(expected.getField1());
        Mockito.when(resultSet.getBigDecimal("FIELD2"))
                .thenReturn(expected.getField2());
    }
}