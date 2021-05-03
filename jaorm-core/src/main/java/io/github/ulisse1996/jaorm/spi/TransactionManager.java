package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Iterator;

public interface TransactionManager {

    Singleton<TransactionManager> INSTANCE = Singleton.instance();

    static TransactionManager getInstance() {
        if (!INSTANCE.isPresent()) {
            Iterator<TransactionManager> services = ServiceFinder.loadServices(TransactionManager.class)
                    .iterator();
            if (services.hasNext()) {
                INSTANCE.set(services.next());
            } else {
                INSTANCE.set(NoOpTransactionManager.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    Transaction getCurrentTransaction();
    void createTransaction();
    DataSourceProvider createDelegate(DataSourceProvider provider);

    class NoOpTransactionManager implements TransactionManager {

        public static final TransactionManager INSTANCE = new NoOpTransactionManager();

        private NoOpTransactionManager() {}

        @Override
        public Transaction getCurrentTransaction() {
            return NoOpTransaction.INSTANCE;
        }

        @Override
        public void createTransaction() {
            // Nothing here
        }

        @Override
        public DataSourceProvider createDelegate(DataSourceProvider provider) {
            throw new UnsupportedOperationException("createDelegate is not supported");
        }

        private static class NoOpTransaction implements Transaction {

            private static final Transaction INSTANCE = new NoOpTransaction();

            private NoOpTransaction() {}

            @Override
            public Status getStatus() {
                return Status.NONE;
            }

            @Override
            public void begin() {
                // No Op
            }

            @Override
            public void commit() {
                // No Op
            }

            @Override
            public void rollback() {
                // No Op
            }
        }
    }
}
