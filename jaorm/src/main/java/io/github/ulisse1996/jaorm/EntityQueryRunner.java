package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.mapping.EmptyClosable;
import io.github.ulisse1996.jaorm.mapping.ResultSetStream;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EntityQueryRunner extends QueryRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        try {
            DelegatesService.getInstance().searchDelegate(klass);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R read(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            executor.getResultSet().next();
            EntityDelegate<?> entityDelegate = delegateSupplier.get();
            entityDelegate.setEntity(executor.getResultSet());
            return (R) entityDelegate;
        } catch (SQLException ex) {
            logger.error(String.format("Error during read for entity %s", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Result<R> readOpt(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            if (executor.getResultSet().next()) {
                EntityDelegate<?> entityDelegate = delegateSupplier.get();
                entityDelegate.setEntity(executor.getResultSet());
                return (Result<R>) Result.of(entityDelegate);
            } else {
                return Result.empty();
            }
        } catch (SQLException ex) {
            logger.error(String.format("Error during readOpt for entity %s", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> readAll(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        List<R> values = new ArrayList<>();
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            while (executor.getResultSet().next()) {
                EntityDelegate<?> entityDelegate = delegateSupplier.get();
                entityDelegate.setEntity(executor.getResultSet());
                values.add((R) entityDelegate);
            }

            return values;
        } catch (SQLException ex) {
            logger.error(String.format("Error during readAll for entity %s", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Stream<R> readStream(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity);
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params);
            return new ResultSetStream<>(connection, preparedStatement, executor,
                    rs -> (R) delegateSupplier.get().toEntity(rs)).getStream();
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            logger.error(String.format("Error during readStream for entity %s", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public TableRow read(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("Read with TableRow is not supported");
    }

    @Override
    public Optional<TableRow> readOpt(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("ReadOpt with TableRow is not supported");
    }

    @Override
    public Stream<TableRow> readStream(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("ReadStream with TableRow is not supported");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity.getClass());
        EntityDelegate<R> delegate = (EntityDelegate<R>) delegateSupplier.get();
        Map<String, Class<?>> columns = delegate.getAutoGeneratedKeys();
        Map<String,Object> generated = doUpdate(query, params, columns);
        delegate.setFullEntity(entity);
        delegate.setAutoGenerated(generated);
        return (R) delegate;
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
