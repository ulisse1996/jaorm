package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class EntityQueryRunnerTest {

    private final EntityQueryRunner testSubject = new EntityQueryRunner();

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @ParameterizedTest
    @MethodSource("getUnsupported")
    void should_throw_unsupported_exception(Executable executable) {
        Assertions.assertThrows(UnsupportedOperationException.class, executable); //NOSONAR
    }

    @Test
    void should_return_stream() throws SQLException {
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);
        Stream<DelegatesMock.MyEntity> myEntityStream = testSubject.readStream(DelegatesMock.MyEntity.class, "", Collections.emptyList());
        Assertions.assertEquals(0, myEntityStream.count());
    }

    @Test
    void should_throw_sql_exception_for_readStream() {
        try {
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenThrow(SQLException.class);
            testSubject.readStream(DelegatesMock.MyEntity.class, "", Collections.emptyList());
        } catch (SQLException ex) {
            Assertions.fail(ex);
        } catch (JaormSqlException ex) {
            Assertions.assertTrue(ex.getCause() instanceof SQLException);
        }
    }

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
        Assertions.assertTrue(testSubject.isCompatible(DelegatesMock.MyEntity.class));
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
            testSubject.read(DelegatesMock.MyEntity.class, "", Collections.emptyList());
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
            testSubject.readOpt(DelegatesMock.MyEntity.class, "", Collections.emptyList());
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
            testSubject.readAll(DelegatesMock.MyEntity.class, "", Collections.emptyList());
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
            testSubject.insert(new DelegatesMock.MyEntity(), "", Collections.emptyList());
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
            DelegatesMock.MyEntity expected = new DelegatesMock.MyEntity();
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
            DelegatesMock.MyEntity result = testSubject.read(DelegatesMock.MyEntity.class, "", Collections.emptyList());
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
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(false);
            Result<DelegatesMock.MyEntity> result = testSubject.readOpt(DelegatesMock.MyEntity.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isPresent());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_optional_with_value_for_read_opt() {
        try {
            DelegatesMock.MyEntity expected = new DelegatesMock.MyEntity();
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
            Result<DelegatesMock.MyEntity> result = testSubject.readOpt(DelegatesMock.MyEntity.class, "", Collections.emptyList());
            Assertions.assertTrue(result.isPresent());
            checkResult(expected, result.get());
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_list_for_read_all() {
        try {
            DelegatesMock.MyEntity expected = new DelegatesMock.MyEntity();
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
            List<DelegatesMock.MyEntity> result = testSubject.readAll(DelegatesMock.MyEntity.class, "", Collections.emptyList());
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(1, result.size());
            checkResult(expected, result.get(0));
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_return_same_entity_after_insert() {
        try {
            DelegatesMock.MyEntity expected = new DelegatesMock.MyEntity();
            expected.setField1("OK");
            expected.setField2(BigDecimal.ZERO);
            DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            DelegatesMock.MyEntity result = testSubject.insert(expected, "QUERY", Collections.emptyList());
            Assertions.assertNotEquals(expected, result);
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
        } catch (SQLException | JaormSqlException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_do_insert_with_generated_keys() throws SQLException {
        Map<String, Class<?>> autoGenerated = new HashMap<>();
        autoGenerated.put("COL1", BigDecimal.class);
        autoGenerated.put("COL2", Integer.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        GeneratorsService generatorsService = Mockito.mock(GeneratorsService.class);
        EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<GeneratorsService> mkGen = Mockito.mockStatic(GeneratorsService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkGen.when(GeneratorsService::getInstance)
                    .thenReturn(generatorsService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getAutoGenerated())
                    .thenReturn(autoGenerated);
            Mockito.when(generatorsService.needGeneration(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(generatorsService.canGenerateValue(Mockito.any(), Mockito.anyString()))
                    .thenReturn(true);
            Mockito.when(generatorsService.generate(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(BigDecimal.ONE);
            Mockito.when(delegatesService.asInsert(Mockito.any(), Mockito.any()))
                    .thenReturn(io.github.ulisse1996.jaorm.Arguments.empty());
            EntityQueryRunner runner = new EntityQueryRunner() {
                @Override
                protected Map<String, Object> doUpdate(String query, List<SqlParameter> params, Map<String, Class<?>> autoGenerated) {
                    return Collections.emptyMap();
                }
            };
            runner.insert(new Object(), "", Collections.emptyList());
            Assertions.assertTrue(autoGenerated.isEmpty());
            Mockito.verify(delegate, Mockito.times(2))
                    .setAutoGenerated(Mockito.any());
        }
    }

    @Test
    void should_throw_exception_for_generated_keys() throws SQLException {
        Map<String, Class<?>> autoGenerated = new HashMap<>();
        autoGenerated.put("COL1", BigDecimal.class);
        autoGenerated.put("COL2", Integer.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        GeneratorsService generatorsService = Mockito.mock(GeneratorsService.class);
        EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<GeneratorsService> mkGen = Mockito.mockStatic(GeneratorsService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkGen.when(GeneratorsService::getInstance)
                    .thenReturn(generatorsService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getAutoGenerated())
                    .thenReturn(autoGenerated);
            Mockito.when(generatorsService.needGeneration(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(generatorsService.canGenerateValue(Mockito.any(), Mockito.anyString()))
                    .thenReturn(true);
            Mockito.when(generatorsService.generate(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenThrow(SQLException.class);
            EntityQueryRunner runner = new EntityQueryRunner() {
                @Override
                protected Map<String, Object> doUpdate(String query, List<SqlParameter> params, Map<String, Class<?>> autoGenerated) {
                    return Collections.emptyMap();
                }
            };
            Assertions.assertThrows(JaormSqlException.class, () -> runner.insert(new Object(), "", Collections.emptyList())); //NOSONAR
        }
    }

    private static Stream<Arguments> getUnsupported() {
        return Stream.of(
                Arguments.arguments((Executable)() -> new EntityQueryRunner().read("", Collections.emptyList())),
                Arguments.arguments((Executable)() -> new EntityQueryRunner().readOpt("", Collections.emptyList())),
                Arguments.arguments((Executable)() -> new EntityQueryRunner().readStream("", Collections.emptyList())
            )
        );
    }

    private void checkResult(DelegatesMock.MyEntity expected, DelegatesMock.MyEntity result) {
        Assertions.assertTrue(result instanceof EntityDelegate);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expected.getField1(), result.getField1()),
                () -> Assertions.assertEquals(expected.getField2(), result.getField2())
        );
    }

    private void setResultSet(DelegatesMock.MyEntity expected) throws SQLException {
        Mockito.when(resultSet.getString("FIELD1"))
                .thenReturn(expected.getField1());
        Mockito.when(resultSet.getBigDecimal("FIELD2"))
                .thenReturn(expected.getField2());
    }
}
