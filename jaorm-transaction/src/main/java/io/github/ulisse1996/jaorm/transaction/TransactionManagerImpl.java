package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.TransactionManager;

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
