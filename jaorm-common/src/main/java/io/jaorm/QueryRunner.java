package io.jaorm;

import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface QueryRunner {

    static QueryRunner getInstance(Class<?> klass) {
        for (QueryRunner runner : ServiceFinder.loadServices(QueryRunner.class)) {
            if (runner.isCompatible(klass)) {
                return runner;
            }
        }

        throw new IllegalArgumentException("Can't find a matched runner for klass " + klass);
    }

    static QueryRunner getSimple() {
        for (QueryRunner runner : ServiceFinder.loadServices(QueryRunner.class)) {
            if (runner.isSimple()) {
                return runner;
            }
        }

        throw new IllegalArgumentException("Can't find Simple Runner");
    }

    default void doUpdate(String query, List<SqlParameter> params) {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            new UpdateExecutor(preparedStatement, params);
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    boolean isCompatible(Class<?> klass);
    boolean isSimple();
    <R> R read(Class<R> klass, String query, List<SqlParameter> params);
    <R> Optional<R> readOpt(Class<R> klass, String query, List<SqlParameter> params);
    <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params);
    <R> R insert(R entity, String query, List<SqlParameter> params);
    void update(String query, List<SqlParameter> params);
    void delete(String query, List<SqlParameter> params);
}
