package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.mapping.EmptyClosable;
import io.github.ulisse1996.jaorm.mapping.ResultSetStream;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            rs.getResultSet().next();
            return (R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1));
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Result<R> readOpt(Class<R> klass, String query, List<SqlParameter> params) {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            if (rs.getResultSet().next()) {
                return Result.of((R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1)));
            } else {
                return Result.empty();
            }
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params) {
        List<R> values = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            while (rs.getResultSet().next()) {
                values.add((R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1)));
            }
            return values;
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Stream<R> readStream(Class<R> klass, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params);
            return new ResultSetStream<>(connection, preparedStatement, executor,
                    rs -> (R) SqlAccessor.find(klass).getGetter().get(rs, rs.getMetaData().getColumnName(1)))
                    .getStream();
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public TableRow read(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params); //NOSONAR Should close it in tableRow
            return new TableRow(connection, preparedStatement, (ResultSetExecutor) executor);
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public Optional<TableRow> readOpt(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            executor = new ResultSetExecutor(preparedStatement, params); //NOSONAR Should close it in tableRow
            if (((ResultSetExecutor) executor).getResultSet().next()) {
                ((ResultSetExecutor) executor).getResultSet().previous();
                return Optional.of(new TableRow(connection, preparedStatement, (ResultSetExecutor) executor));
            } else {
                SqlUtil.silentClose(executor, preparedStatement, connection);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public Stream<TableRow> readStream(String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params);
            Connection finalConnection = connection;
            PreparedStatement finalPreparedStatement = preparedStatement;
            ResultSetExecutor finalExecutor = (ResultSetExecutor) executor;
            return new ResultSetStream<>(connection, preparedStatement, executor,
                    rs -> new TableRow(finalConnection, finalPreparedStatement, finalExecutor, true)).getStream();
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("Insert on Simple Runner not supported");
    }

    @Override
    public void update(String query, List<SqlParameter> params) {
        doUpdate(query, params, Collections.emptyMap());
    }

    @Override
    public void delete(String query, List<SqlParameter> params) {
        doUpdate(query, params, Collections.emptyMap());
    }
}
