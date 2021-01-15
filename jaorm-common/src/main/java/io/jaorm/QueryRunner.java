package io.jaorm;

import io.jaorm.entity.sql.SqlParameter;

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

    boolean isCompatible(Class<?> klass);
    <R> R read(Class<R> klass, String query, List<SqlParameter> params);
    <R> Optional<R> readOpt(Class<R> klass, String query, List<SqlParameter> params);
    <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params);
}
