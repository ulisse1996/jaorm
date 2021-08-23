package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.common.TransactionSupport;

import javax.sql.DataSource;
import java.util.Iterator;

public abstract class MultiSchemaTransactionManager implements TransactionSupport {

    private static final Singleton<MultiSchemaTransactionManager> INSTANCE = Singleton.instance();

    protected MultiSchemaTransactionManager() {}

    public static synchronized MultiSchemaTransactionManager getInstance() {
        if (INSTANCE.isPresent()) {
            return INSTANCE.get();
        }

        Iterator<MultiSchemaTransactionManager> services = ServiceFinder.loadServices(MultiSchemaTransactionManager.class)
                .iterator();
        if (services.hasNext()) {
            INSTANCE.set(services.next());
        } else {
            INSTANCE.set(NoOpTransactionManager.INSTANCE);
        }

        return INSTANCE.get();
    }

    public DataSourceProvider toProvider(MultiSchemaDatasourceProvider provider, Class<?> delegateClass) {
        return new DataSourceProvider() {
            @Override
            public DataSource getDataSource() {
                return provider.getDatasource(delegateClass);
            }
        };
    }

    public static class NoOpTransactionManager extends MultiSchemaTransactionManager {

        private static final NoOpTransactionManager INSTANCE = new NoOpTransactionManager();

        @Override
        public Transaction getCurrentTransaction() {
            return NoOpTransaction.INSTANCE;
        }

        @Override
        public void createTransaction() {
            throw new UnsupportedOperationException("createTransaction is not supported");
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
