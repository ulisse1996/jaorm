package io.github.ulisse1996;

import io.github.ulisse1996.entity.sql.DataSourceProvider;
import io.github.ulisse1996.entity.sql.SqlParameter;
import io.github.ulisse1996.spi.DelegatesService;
import io.github.ulisse1996.spi.QueryRunner;
import io.github.ulisse1996.spi.TransactionManager;
import io.github.ulisse1996.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

class QueryRunnerTest {

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
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            mk.when(() -> ServiceFinder.loadService(DelegatesService.class))
                    .thenReturn(new DelegatesMock());
            QueryRunner runner = QueryRunner.getInstance(DelegatesMock.MyEntity.class);
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_call_service_load_for_entity_runner_only_first_time() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            mk.when(() -> ServiceFinder.loadService(DelegatesService.class))
                    .thenReturn(new DelegatesMock());
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

                Map<String,Object> map = doUpdate(query, params, autoGen);
                Assertions.assertFalse(map.isEmpty());
                Assertions.assertEquals(expected, map);
                return entity;
            }

            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
        runner.insert(new Object(), "", Collections.emptyList());
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

    private QueryRunner buildRunner() {
        return new MockedRunner() {
            @Override
            public void update(String query, List<SqlParameter> params) {
                try {
                    getConnection();
                } catch (Exception ex) {
                    Assertions.fail(ex);
                }
            }
        };
    }
}
