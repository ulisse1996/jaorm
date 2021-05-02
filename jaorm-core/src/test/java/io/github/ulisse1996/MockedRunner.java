package io.github.ulisse1996;

import io.github.ulisse1996.entity.sql.SqlParameter;
import io.github.ulisse1996.mapping.TableRow;
import io.github.ulisse1996.spi.QueryRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MockedRunner extends QueryRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        return DelegatesMock.MyEntity.class.equals(klass);
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
    public <R> Stream<R> readStream(Class<R> klass, String query, List<SqlParameter> params) {
        return null;
    }

    @Override
    public TableRow read(String query, List<SqlParameter> params) {
        return null;
    }

    @Override
    public Optional<TableRow> readOpt(String query, List<SqlParameter> params) {
        return Optional.empty();
    }

    @Override
    public Stream<TableRow> readStream(String query, List<SqlParameter> params) {
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
