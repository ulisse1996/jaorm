package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.TransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

class DataSourceProviderDelegateTest {

    @Test
    void should_return_proxy_datasource() {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        TransactionImpl transaction = new TransactionImpl();
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(Mockito.mock(DataSourceProvider.class));
            DataSource dataSource = providerDelegate.getDataSource();
            Assertions.assertTrue(Proxy.isProxyClass(dataSource.getClass()));
        }
    }

    @Test
    void should_return_proxy_datasource_for_schema() {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        TransactionImpl transaction = new TransactionImpl();
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(Mockito.mock(DataSourceProvider.class));
            DataSource dataSource = providerDelegate.getDataSource(TableInfo.EMPTY);
            Assertions.assertTrue(Proxy.isProxyClass(dataSource.getClass()));
        }
    }

    @Test
    void should_invoke_datasource_method_for_empty_transaction() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(null);
            Mockito.when(dataSourceProvider.getDataSource())
                    .thenReturn(dataSource);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(dataSourceProvider);
            DataSource result = providerDelegate.getDataSource();
            Assertions.assertTrue(Proxy.isProxyClass(result.getClass()));
            Assertions.assertDoesNotThrow(result::getLogWriter);
            Mockito.verify(dataSource, Mockito.times(1))
                    .getLogWriter();
        }
    }

    @Test
    void should_get_real_connection_for_empty_transaction() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(null);
            Mockito.when(dataSourceProvider.getDataSource())
                    .thenReturn(dataSource);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(dataSourceProvider);
            DataSource result = providerDelegate.getDataSource();
            Assertions.assertTrue(Proxy.isProxyClass(result.getClass()));
            Assertions.assertDoesNotThrow((ThrowingSupplier<Connection>) result::getConnection);
            Mockito.verify(dataSource, Mockito.times(1))
                    .getConnection();
        }
    }

    @Test
    void should_return_proxy_connection() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(dataSourceProvider.getDataSource())
                    .thenReturn(dataSource);
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(dataSourceProvider);
            Connection result = providerDelegate.getConnection();
            Assertions.assertTrue(Proxy.isProxyClass(result.getClass()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Transaction.Status.class, names = {"COMMIT", "ROLLBACK"})
    void should_call_close_for_transaction_status(Transaction.Status status) throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(dataSourceProvider.getDataSource())
                    .thenReturn(dataSource);
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(dataSourceProvider);
            Connection result = providerDelegate.getConnection();
            setStatus(transaction, status);
            Assertions.assertTrue(Proxy.isProxyClass(result.getClass()));
            result.close();
            Mockito.verify(connection, Mockito.times(2))
                    .close();
        }
    }

    @Test
    void should_not_call_close_on_open_transaction() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.when(dataSourceProvider.getDataSource())
                    .thenReturn(dataSource);
            Mockito.when(dataSource.getConnection())
                    .thenReturn(connection);
            DataSourceProviderDelegate providerDelegate =
                    new DataSourceProviderDelegate(dataSourceProvider);
            Connection result = providerDelegate.getConnection();
            Assertions.assertTrue(Proxy.isProxyClass(result.getClass()));
            result.close();
            Mockito.verify(connection, Mockito.times(0))
                    .close();
        }
    }

    private void setStatus(TransactionImpl transaction, Transaction.Status status) {
        try {
            Field declaredField = transaction.getClass().getDeclaredField("status");
            declaredField.setAccessible(true);
            declaredField.set(transaction, status);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
}
