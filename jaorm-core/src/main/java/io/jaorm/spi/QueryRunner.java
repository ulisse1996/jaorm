package io.jaorm.spi;

import io.jaorm.ResultSetExecutor;
import io.jaorm.ServiceFinder;
import io.jaorm.UpdateExecutor;
import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;
import io.jaorm.logger.JaormLogger;
import io.jaorm.logger.SqlJaormLogger;
import io.jaorm.spi.common.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class QueryRunner {

    protected static final SqlJaormLogger logger = JaormLogger.getSqlLogger(ResultSetExecutor.class);
    private static final Singleton<QueryRunner> ENTITY_RUNNER = Singleton.instance();
    private static final Singleton<QueryRunner> SIMPLE_RUNNER = Singleton.instance();

    public static QueryRunner getInstance(Class<?> klass) {
        if (!ENTITY_RUNNER.isPresent()) {
            for (QueryRunner runner : ServiceFinder.loadServices(QueryRunner.class)) {
                if (runner.isCompatible(klass)) {
                    ENTITY_RUNNER.set(runner);
                }
            }

            if (ENTITY_RUNNER.isPresent()) {
                return ENTITY_RUNNER.get();
            }
        } else {
            return ENTITY_RUNNER.get();
        }

        throw new IllegalArgumentException("Can't find a matched runner for klass " + klass);
    }

    public static QueryRunner getSimple() {
        if (!SIMPLE_RUNNER.isPresent()) {
            for (QueryRunner runner : ServiceFinder.loadServices(QueryRunner.class)) {
                if (runner.isSimple()) {
                    SIMPLE_RUNNER.set(runner);
                }
            }

            if (SIMPLE_RUNNER.isPresent()) {
                return SIMPLE_RUNNER.get();
            }
        } else {
            return SIMPLE_RUNNER.get();
        }

        throw new IllegalArgumentException("Can't find Simple Runner");
    }

    protected void doUpdate(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            new UpdateExecutor(preparedStatement, params);
        } catch (SQLException ex) {
            logger.error("Error during update/insert/delete"::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    protected Connection getConnection() throws SQLException {
        DataSourceProvider provider = DataSourceProvider.getCurrent();
        TransactionManager manager = TransactionManager.getInstance();
        if (manager instanceof TransactionManager.NoOpTransactionManager || manager.getCurrentTransaction() == null) {
            return provider.getConnection();
        } else {
            DataSourceProvider delegate = DataSourceProvider.getCurrentDelegate();
            if (delegate == null) {
                DataSourceProvider.setDelegate(manager.createDelegate(provider));
                return DataSourceProvider.getCurrentDelegate().getConnection();
            } else {
                return delegate.getConnection();
            }
        }
    }

    public abstract boolean isCompatible(Class<?> klass);
    public abstract boolean isSimple();
    public abstract <R> R read(Class<R> klass, String query, List<SqlParameter> params);
    public abstract <R> Optional<R> readOpt(Class<R> klass, String query, List<SqlParameter> params);
    public abstract <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params);
    public abstract <R> R insert(R entity, String query, List<SqlParameter> params);
    public abstract void update(String query, List<SqlParameter> params);
    public abstract void delete(String query, List<SqlParameter> params);
}
