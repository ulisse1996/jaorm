package io.jaorm;

import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.spi.QueryRunner;
import io.jaorm.spi.TransactionManager;
import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    void should_not_find_query_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertThrows(IllegalArgumentException.class, () -> QueryRunner.getInstance(Object.class));
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
            QueryRunner runner = QueryRunner.getInstance(String.class);
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_call_service_load_for_entity_runner_only_first_time() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            QueryRunner runner = QueryRunner.getInstance(String.class);
            Assertions.assertEquals(expected, runner);
            QueryRunner runner1 = QueryRunner.getInstance(String.class);
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
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(provider.isDelegate())
                    .thenReturn(true);
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
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(provider.isDelegate())
                    .thenReturn(false);
            Mockito.when(provider.getDataSource())
                    .thenReturn(source);
            Mockito.when(source.getConnection())
                    .thenReturn(connection);
            Assertions.assertDoesNotThrow(() -> runner.update("UPDATE", Collections.emptyList()));
            Mockito.verify(provider, Mockito.times(1))
                    .setInstance(Mockito.any());
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