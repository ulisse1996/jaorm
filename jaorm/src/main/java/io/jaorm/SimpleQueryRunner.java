package io.jaorm;

import io.jaorm.entity.sql.DatasourceProvider;
import io.jaorm.entity.sql.SqlAccessor;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleQueryRunner implements QueryRunner {

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
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
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
    public <R> Optional<R> readOpt(Class<R> klass, String query, List<SqlParameter> params) {
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(query);
             ResultSetExecutor rs = new ResultSetExecutor(pr, params)) {
            if (rs.getResultSet().next()) {
                return Optional.of((R) SqlAccessor.find(klass).getGetter().get(rs.getResultSet(), rs.getResultSet().getMetaData().getColumnName(1)));
            } else {
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> readAll(Class<R> klass, String query, List<SqlParameter> params) {
        List<R> values = new ArrayList<>();
        try (Connection connection = DatasourceProvider.getCurrent().getConnection();
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
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("Insert on Simple Runner not supported");
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
