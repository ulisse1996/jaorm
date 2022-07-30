package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

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
    public <R> Result<R> readOpt(Class<R> klass, String query, List<SqlParameter> params) {
        return Result.empty();
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
    public <R> Cursor<R> readCursor(Class<R> klass, String query, List<SqlParameter> parameters) {
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
    public <R> List<R> insertWithBatch(Class<?> entityClass, String query, List<R> entities) {
        return null;
    }

    @Override
    public int update(String query, List<SqlParameter> params) {
        return 0;
    }

    @Override
    public int delete(String query, List<SqlParameter> params) {
        return 0;
    }

    @Override
    public <R> List<R> updateWithBatch(Class<?> entityClass, String updateSql, List<R> entities) {
        return null;
    }
}
