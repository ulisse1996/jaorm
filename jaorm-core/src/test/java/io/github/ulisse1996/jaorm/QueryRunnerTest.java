package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.TransactionManager;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;
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
import java.util.*;

@ExtendWith(MockitoExtension.class)
class QueryRunnerTest {

    @Mock private DelegatesService delegatesService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        // Reset all singleton
        try {
            Field simpleRunner = QueryRunner.class.getDeclaredField("SIMPLE_RUNNER");
            Field entityRunner = QueryRunner.class.getDeclaredField("ENTITY_RUNNER");
            simpleRunner.setAccessible(true);
            entityRunner.setAccessible(true);
            ((Singleton<QueryRunner>)simpleRunner.get(null)).set(null);
            ((Singleton<QueryRunner>)entityRunner.get(null)).set(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    void should_return_same_rows() {
        MockedRunner runner = new MockedRunner();
        runner.registerUpdatedRows(1, 10);
        Assertions.assertEquals(10, runner.getUpdatedRows(1));
    }

    @Test
    void should_not_find_simple_query_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.emptyList());
            mk.when(() -> ServiceFinder.loadService(DelegatesService.class))
                    .thenReturn(new DelegatesMock());
            Assertions.assertThrows(IllegalArgumentException.class, () -> QueryRunner.getInstance(Object.class));
        }
    }

    @Test
    void should_not_find_query_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.emptyList());
            mk.when(() -> ServiceFinder.loadService(DelegatesService.class))
                    .thenReturn(new DelegatesMock());
            Assertions.assertThrows(IllegalArgumentException.class, () -> QueryRunner.getInstance(DelegatesMock.MyEntity.class));
        }
    }

    @Test
    void should_not_find_simple_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.singletonList(new MockedRunner()));
            Assertions.assertThrows(IllegalArgumentException.class, QueryRunner::getSimple);
        }
    }

    @Test
    void should_find_simple_runner() {
        SimpleMockedRunner expected = new SimpleMockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.singletonList(expected));
            QueryRunner runner = QueryRunner.getSimple();
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_find_custom_runner() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
             MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            mkDel.when(DelegatesService::getInstance).thenReturn(new DelegatesMock());
            QueryRunner runner = QueryRunner.getInstance(DelegatesMock.MyEntity.class);
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_call_service_load_for_entity_runner_only_first_time() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            mkDel.when(DelegatesService::getInstance).thenReturn(new DelegatesMock());
            QueryRunner runner = QueryRunner.getInstance(DelegatesMock.MyEntity.class);
            Assertions.assertEquals(expected, runner);
            QueryRunner runner1 = QueryRunner.getInstance(DelegatesMock.MyEntity.class);
            Assertions.assertEquals(runner, runner1);
            mk.verify(() -> ServiceFinder.loadServices(QueryRunner.class));
        }
    }

    @Test
    void should_call_service_load_for_simple_runner_only_first_time() {
        SimpleMockedRunner expected = new SimpleMockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            QueryRunner runner = QueryRunner.getSimple();
            Assertions.assertEquals(expected, runner);
            QueryRunner runner1 = QueryRunner.getSimple();
            Assertions.assertEquals(runner, runner1);
            mk.verify(() -> ServiceFinder.loadServices(QueryRunner.class));
        }
    }

    @Test
    void should_return_map_of_auto_generated_keys() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.any(String[].class)))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.getGeneratedKeys())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, false);
        Mockito.when(resultSet.getString("NAME1"))
                .thenReturn("RETURN1");
        Mockito.when(resultSet.getString("NAME2"))
                .thenReturn("RETURN2");

        QueryRunner runner = new MockedRunner() {
            @Override
            public <R> R insert(R entity, String query, List<SqlParameter> params) {
                Map<String, Object> expected = new HashMap<>();
                expected.put("NAME1", "RETURN1");
                expected.put("NAME2", "RETURN2");

                Map<String, Class<?>> autoGen = new HashMap<>();
                autoGen.put("NAME1", String.class);
                autoGen.put("NAME2", String.class);

                Map<String,Object> map = doUpdate(entity.getClass(), query, params, autoGen);
                Assertions.assertFalse(map.isEmpty());
                Assertions.assertEquals(expected, map);
                return entity;
            }

            @Override
            public Connection getConnection(TableInfo tableInfo) {
                return connection;
            }
        };
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(GeneratedKeysSpecific.class, GeneratedKeysSpecific.NO_OP))
                    .thenReturn(new Generated());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getTableInfo(Mockito.any()))
                    .thenReturn(TableInfo.EMPTY);
            runner.insert(new Object(), "", Collections.emptyList());
        }
    }

    @Test
    void should_get_simple_connection_from_delegate() throws SQLException {
        DataSourceProvider provider = Mockito.spy(DataSourceProvider.class);
        QueryRunner runner = buildRunner();
        Connection connection = Mockito.mock(Connection.class);
        DataSource source = Mockito.mock(DataSource.class);
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class);
             MockedStatic<TransactionManager> mkManager = Mockito.mockStatic(TransactionManager.class)) {
            mkManager.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            mk.when(DataSourceProvider::getCurrentDelegate)
                    .thenReturn(provider);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(provider.getDataSource())
                    .thenReturn(source);
            Mockito.when(source.getConnection())
                    .thenReturn(connection);
            Assertions.assertDoesNotThrow(() -> runner.update("UPDATE", Collections.emptyList()));
        }
    }

    @Test
    void should_create_delegate() throws SQLException {
        DataSourceProvider provider = Mockito.spy(DataSourceProvider.class);
        QueryRunner runner = buildRunner();
        Connection connection = Mockito.mock(Connection.class);
        DataSource source = Mockito.mock(DataSource.class);
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class);
             MockedStatic<TransactionManager> mkManager = Mockito.mockStatic(TransactionManager.class)) {
            mkManager.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            mk.when(DataSourceProvider::getCurrentDelegate)
                    .thenReturn(null)
                    .thenReturn(provider);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(provider.getDataSource())
                    .thenReturn(source);
            Mockito.when(source.getConnection())
                    .thenReturn(connection);
            Assertions.assertDoesNotThrow(() -> runner.update("UPDATE", Collections.emptyList()));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_simple_runner() {
        QueryRunner mock = Mockito.mock(QueryRunner.class);
        DelegatesService mockDelegates = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(mockDelegates);
            Field field = QueryRunner.class.getDeclaredField("SIMPLE_RUNNER");
            field.setAccessible(true);
            Singleton<QueryRunner> instance = (Singleton<QueryRunner>) field.get(null);
            instance.set(mock);

            Mockito.when(mockDelegates.getDelegates())
                    .thenReturn(Collections.emptyMap());

            QueryRunner res = QueryRunner.getInstance(Object.class);
            Assertions.assertEquals(mock, res);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_generate_keys_with_alternative_method() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.any(String[].class)))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.getGeneratedKeys())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);
        Mockito.when(resultSet.next())
                .thenReturn(true, false);
        Mockito.when(resultSet.getString("NAME1"))
                .thenThrow(customSql());
        Mockito.when(resultSet.getString("NAME2"))
                .thenThrow(customSql());
        Mockito.when(metaData.getColumnCount())
                .thenReturn(2);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("NAME1");
        Mockito.when(metaData.getColumnName(2))
                .thenReturn("NAME2");
        Mockito.when(resultSet.getObject(1, String.class))
                .thenReturn("RETURN1");
        Mockito.when(resultSet.getObject(2, String.class))
                .thenReturn("RETURN2");

        QueryRunner runner = new MockedRunner() {
            @Override
            public <R> R insert(R entity, String query, List<SqlParameter> params) {
                Map<String, Object> expected = new HashMap<>();
                expected.put("NAME1", "RETURN1");
                expected.put("NAME2", "RETURN2");

                Map<String, Class<?>> autoGen = new HashMap<>();
                autoGen.put("NAME1", String.class);
                autoGen.put("NAME2", String.class);

                Map<String,Object> map = doUpdate(entity.getClass(), query, params, autoGen);
                Assertions.assertFalse(map.isEmpty());
                Assertions.assertEquals(expected, map);
                return entity;
            }

            @Override
            public Connection getConnection(TableInfo tableInfo) {
                return connection;
            }
        };
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(GeneratedKeysSpecific.class, GeneratedKeysSpecific.NO_OP))
                    .thenReturn(new Generated());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getTableInfo(Mockito.any()))
                    .thenReturn(TableInfo.EMPTY);
            runner.insert(new Object(), "", Collections.emptyList());
        }
    }

    private Throwable customSql() {
        return new SQLException("", "", 17023);
    }

    @Test
    void should_throw_exception_for_generate_keys() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.any(String[].class)))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.getGeneratedKeys())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, false);
        Mockito.when(resultSet.getString("NAME2"))
                .thenThrow(SQLException.class);

        QueryRunner runner = new MockedRunner() {
            @Override
            public <R> R insert(R entity, String query, List<SqlParameter> params) {
                Map<String, Object> expected = new HashMap<>();
                expected.put("NAME1", "RETURN1");
                expected.put("NAME2", "RETURN2");

                Map<String, Class<?>> autoGen = new HashMap<>();
                autoGen.put("NAME1", String.class);
                autoGen.put("NAME2", String.class);

                Map<String,Object> map = doUpdate(entity.getClass(), query, params, autoGen);
                Assertions.assertFalse(map.isEmpty());
                Assertions.assertEquals(expected, map);
                return entity;
            }

            @Override
            public Connection getConnection(TableInfo tableInfo) {
                return connection;
            }
        };
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(GeneratedKeysSpecific.class, GeneratedKeysSpecific.NO_OP))
                    .thenReturn(new Generated());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getTableInfo(Mockito.any()))
                    .thenReturn(TableInfo.EMPTY);
            Assertions.assertThrows(JaormSqlException.class, () -> runner.insert(new Object(), "", Collections.emptyList())); //NOSONAR
        }
    }

    private QueryRunner buildRunner() {
        return new MockedRunner() {
            @Override
            public int update(String query, List<SqlParameter> params) {
                try {
                    getConnection(TableInfo.EMPTY);
                } catch (Exception ex) {
                    Assertions.fail(ex);
                }
                return 0;
            }
        };
    }

    private static class Generated implements GeneratedKeysSpecific {

        @Override
        public String getReturningKeys(Set<String> keys) {
            return "";
        }

        @Override
        public boolean isCustomReturnKey() {
            return false;
        }

        @Override
        public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) throws SQLException {
            return null;
        }
    }
}
