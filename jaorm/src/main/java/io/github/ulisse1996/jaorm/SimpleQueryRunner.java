package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.mapping.*;
import io.github.ulisse1996.jaorm.metrics.MetricInfo;
import io.github.ulisse1996.jaorm.metrics.TimeTracker;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.MetricsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SimpleQueryRunner extends QueryRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        try {
            SqlAccessor.find(klass);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R read(Class<R> klass, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        TimeTracker tracker = TimeTracker.start();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            rs.getResultSet().next();
            tracker.stop();
            return (R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1));
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Result<R> readOpt(Class<R> klass, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        TimeTracker tracker = TimeTracker.start();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            if (rs.getResultSet().next()) {
                tracker.stop();
                return Result.of((R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1)));
            } else {
                tracker.stop();
                return Result.empty();
            }
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        List<R> values = new ArrayList<>();
        TimeTracker tracker = TimeTracker.start();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            while (rs.getResultSet().next()) {
                values.add((R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1)));
            }
            tracker.stop();
            return values;
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    public <R> Stream<R> readStream(Class<R> klass, String query, List<SqlParameter> params) {
        return produceIterableResult(klass, query, params, ResultSetStream::new).getStream();
    }

    @Override
    public <R> Cursor<R> readCursor(Class<R> klass, String query, List<SqlParameter> params) {
        return produceIterableResult(klass, query, params, JaormCursor::new);
    }

    @SuppressWarnings({"unchecked", "java:S2093"})
    private <T, R> T produceIterableResult(Class<R> klass, String query, List<SqlParameter> params,
                                         JaormIterableResultProducer<T, R> producer) {
        ThrowingFunction<ResultSet, R, SQLException> mapper =
                rs -> (R) SqlAccessor.find(klass).getGetter().get(rs, rs.getMetaData().getColumnName(1));
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        TimeTracker tracker = TimeTracker.start();
        try {
            connection = getConnection(TableInfo.EMPTY);
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params); //NOSONAR We return an instance of closeable (TableRow and Stream) that user must close
            tracker.stop();
            return producer.produce(connection, preparedStatement, executor, mapper);
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    @SuppressWarnings("java:S2093")
    public TableRow read(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        CloseableTuple tuple = new CloseableTuple();
        TimeTracker tracker = TimeTracker.start();
        try {
            tuple.connection = getConnection(TableInfo.EMPTY);
            tuple.preparedStatement = tuple.connection.prepareStatement(query);
            tuple.executor = new ResultSetExecutor(tuple.preparedStatement, params); //NOSONAR Should close it in tableRow
            tracker.stop();
            return new TableRow(tuple.connection, tuple.preparedStatement, (ResultSetExecutor) tuple.executor); //NOSONAR We return an instance of closeable (TableRow) that user must close
        } catch (SQLException ex) {
            tuple.close();
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    @SuppressWarnings("java:S2093")
    public Optional<TableRow> readOpt(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        CloseableTuple tuple = new CloseableTuple();
        TimeTracker tracker = TimeTracker.start();
        try {
            tuple.connection = getConnection(TableInfo.EMPTY);
            tuple.preparedStatement = tuple.connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            tuple.executor = new ResultSetExecutor(tuple.preparedStatement, params); //NOSONAR Should close it in tableRow
            if (((ResultSetExecutor) tuple.executor).getResultSet().next()) {
                ((ResultSetExecutor) tuple.executor).getResultSet().previous();
                tracker.stop();
                return Optional.of(new TableRow(tuple.connection, tuple.preparedStatement, (ResultSetExecutor) tuple.executor)); //NOSONAR We return an instance of closeable (TableRow) that user must close
            } else {
                tuple.close();
                tracker.stop();
                return Optional.empty();
            }
        } catch (SQLException ex) {
            tuple.close();
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    @SuppressWarnings("java:S2093")
    public Stream<TableRow> readStream(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        TimeTracker tracker = TimeTracker.start();
        try {
            connection = getConnection(TableInfo.EMPTY);
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params);
            Connection finalConnection = connection;
            PreparedStatement finalPreparedStatement = preparedStatement;
            ResultSetExecutor finalExecutor = (ResultSetExecutor) executor;
            tracker.stop();
            return new ResultSetStream<>(connection, preparedStatement, executor, //NOSONAR We return an instance of closeable (TableRow) that user must close
                    rs -> new TableRow(finalConnection, finalPreparedStatement, finalExecutor, true)).getStream();
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        } finally {
            MetricsService.getInstance().trackExecution(MetricInfo.of(query, params, !tracker.isStopped(), tracker.getElapsed()));
        }
    }

    @Override
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("Insert on Simple Runner not supported");
    }

    @Override
    public <R> List<R> insertWithBatch(Class<?> entityClass, String query, List<R> entities) {
        throw new UnsupportedOperationException("Insert on Simple Runner not supported");
    }

    @Override
    public <R> List<R> updateWithBatch(Class<?> entityClass, String updateSql, List<R> entities) {
        throw new UnsupportedOperationException("Update Batch on Simple Runner not supported");
    }

    @Override
    public int update(String query, List<SqlParameter> params) {
        return doSimpleUpdate(query, params);
    }

    @Override
    public int delete(String query, List<SqlParameter> params) {
        return doSimpleUpdate(query, params);
    }

    private static class CloseableTuple {
        private Connection connection;
        private PreparedStatement preparedStatement;
        private SqlExecutor executor;

        private CloseableTuple() {
            this.connection = EmptyClosable.instance(Connection.class);
            this.preparedStatement = EmptyClosable.instance(PreparedStatement.class);
            this.executor = EmptyClosable.instance(SqlExecutor.class);
        }

        public void close() {
            SqlUtil.silentClose(this.executor, this.preparedStatement, this.connection);
        }
    }
}
