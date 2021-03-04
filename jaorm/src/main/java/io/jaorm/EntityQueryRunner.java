package io.jaorm;

import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
    public <R> Optional<R> readOpt(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            if (executor.getResultSet().next()) {
                EntityDelegate<?> entityDelegate = delegateSupplier.get();
                entityDelegate.setEntity(executor.getResultSet());
                return (Optional<R>) Optional.of(entityDelegate);
            } else {
                return Optional.empty();
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
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        doUpdate(query, params);
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity.getClass());
        EntityDelegate<R> delegate = (EntityDelegate<R>) delegateSupplier.get();
        delegate.setFullEntity(entity);
        return (R) delegate;
    }

    @Override
    public void update(String query, List<SqlParameter> params) {
        doUpdate(query, params);
    }

    @Override
    public void delete(String query, List<SqlParameter> params) {
        doUpdate(query, params);
    }
}
