package io.jaorm.transaction;

import io.jaorm.Transaction;
import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.spi.TransactionManager;

public class TransactionManagerImpl implements TransactionManager {

    static final ThreadLocal<Transaction> TRANSACTION_THREAD_LOCAL =
            new InheritableThreadLocal<>();

    @Override
    public synchronized Transaction getCurrentTransaction() {
        return TRANSACTION_THREAD_LOCAL.get();
    }

    @Override
    public synchronized void createTransaction() {
        TRANSACTION_THREAD_LOCAL.set(new TransactionImpl());
    }

    @Override
    public DataSourceProvider createDelegate(DataSourceProvider provider) {
        return new DataSourceProviderDelegate(provider);
    }
}
