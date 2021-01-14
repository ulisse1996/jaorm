package io.jaorm;

import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.sql.DatasourceProvider;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class QueryRunner {

    private QueryRunner() {}

    @SuppressWarnings("unchecked")
    public static <R> R read(Class<R> entity, String query, List<SqlParameter> params) {
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getCurrent().searchDelegate(entity);
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            executor.getResultSet().next();
            EntityDelegate<?> entityDelegate = delegateSupplier.get();
            entityDelegate.setEntity(executor.getResultSet());
            return (R) entityDelegate;
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> Optional<R> readOpt(Class<R> entity, String query, List<SqlParameter> params) {
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getCurrent().searchDelegate(entity);
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
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
            throw new JaormSqlException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> List<R> readAll(Class<R> entity, String query, List<SqlParameter> params) {
        List<R> values = new ArrayList<>();
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getCurrent().searchDelegate(entity);
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            while (executor.getResultSet().next()) {
                EntityDelegate<?> entityDelegate = delegateSupplier.get();
                entityDelegate.setEntity(executor.getResultSet());
                values.add((R) entityDelegate);
            }

            return values;
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }
}
