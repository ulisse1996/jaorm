package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.MultiSchemaDatasourceProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionImpl implements Transaction {

    private static final JaormLogger logger = JaormLogger.getLogger(TransactionImpl.class);
    private final boolean multiSchema;
    private Status status;
    private Connection connection;

    public TransactionImpl() {
        this(false);
    }

    public TransactionImpl(boolean multiSchema) {
        this.status = Status.NONE;
        this.multiSchema = multiSchema;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    public void start(Connection connection) throws SQLException {
        this.connection = connection;
        begin();
    }

    @Override
    public void begin() throws SQLException {
        logger.debug("Starting Transaction"::toString);
        this.connection.setAutoCommit(false);
        this.status = Status.STARTED;
    }

    @Override
    public void commit() throws SQLException {
        logger.debug("Commit on current Transaction"::toString);
        this.connection.commit();
        this.status = Status.COMMIT;
        close();
    }

    @Override
    public void rollback() throws SQLException {
        logger.debug("Rollback on current Transaction"::toString);
        this.connection.rollback();
        this.status = Status.ROLLBACK;
        close();
    }

    private void close() {
        try {
            this.connection.close();
        } catch (SQLException ignored) {
            // Ignored
        }
        if (multiSchema) {
            MultiSchemaTransactionManagerImpl.TRANSACTION_THREAD_LOCAL.remove();
            MultiSchemaDatasourceProvider.clearDelegates();
        } else {
            TransactionManagerImpl.TRANSACTION_THREAD_LOCAL.remove();
            DataSourceProvider.setDelegate(null);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
