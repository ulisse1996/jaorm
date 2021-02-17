package io.jaorm;

import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.spi.QueryRunner;

import java.util.List;
import java.util.Optional;

public class MockedRunner implements QueryRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        return klass.equals(String.class);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public <R> R read(Class<R> klass, String query, List<SqlParameter> params) {
        return null;
    }

    @Override
    public <R> Optional<R> readOpt(Class<R> klass, String query, List<SqlParameter> params) {
        return Optional.empty();
    }

    @Override
    public <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params) {
        return null;
    }

    @Override
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        return null;
    }

    @Override
    public void update(String query, List<SqlParameter> params) {

    }

    @Override
    public void delete(String query, List<SqlParameter> params) {

    }
}
