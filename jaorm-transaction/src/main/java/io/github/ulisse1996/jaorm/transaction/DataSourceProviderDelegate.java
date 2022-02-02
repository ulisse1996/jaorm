package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.TransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataSourceProviderDelegate extends DataSourceProvider {

    private final DataSourceProvider instance;
    private DataSource proxyInstance;
    private final Map<String, DataSource> schemaProxies;

    public DataSourceProviderDelegate(DataSourceProvider instance) {
        this.instance = instance;
        this.schemaProxies = new HashMap<>();
    }

    @Override
    public DataSource getDataSource() {
        if (proxyInstance == null) {
            DataSource dataSource = this.instance.getDataSource();
            this.proxyInstance = createProxyDataSource(dataSource);
        }

        return proxyInstance;
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        if (!this.schemaProxies.containsKey(tableInfo.getSchema())) {
            DataSource dataSource = this.instance.getDataSource(tableInfo);
            this.schemaProxies.put(tableInfo.getSchema(), createProxyDataSource(dataSource));
        }

        return this.schemaProxies.get(tableInfo.getSchema());
    }

    private DataSource createProxyDataSource(DataSource dataSource) {
        TransactionImpl currentTransaction = (TransactionImpl) TransactionManager.getInstance().getCurrentTransaction();
        return (DataSource) Proxy.newProxyInstance(this.instance.getClass().getClassLoader(), new Class[]{DataSource.class},
                (proxy, method, args) -> {
                    if (method.getName().contains("getConnection")) {
                        if (currentTransaction == null) {
                            return method.invoke(dataSource, args); // We are not in a transaction
                        }

                        if (!currentTransaction.getStatus().equals(Transaction.Status.STARTED)) {
                            currentTransaction.start(createProxyConnection((Connection) method.invoke(dataSource, args)));
                        }
                        return currentTransaction.getConnection();
                    }

                    return method.invoke(dataSource, args);
                });
    }

    private Connection createProxyConnection(Connection connection) {
        Transaction currentTransaction = TransactionManager.getInstance().getCurrentTransaction();
        return (Connection) Proxy.newProxyInstance(this.instance.getClass().getClassLoader(), new Class[] {Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().contains("close")) {
                        if (Arrays.asList(Transaction.Status.COMMIT, Transaction.Status.ROLLBACK).contains(currentTransaction.getStatus())) {
                            connection.close();
                        } else {
                            return null; // We do nothing if we need close but current transaction is still up
                        }
                    }

                    return method.invoke(connection, args);
                });
    }
}
