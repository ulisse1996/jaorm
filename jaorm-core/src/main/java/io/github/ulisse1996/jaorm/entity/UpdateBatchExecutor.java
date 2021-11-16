package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.SqlExecutor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UpdateBatchExecutor implements SqlExecutor {
    private final ResultSet resultSet;

    public UpdateBatchExecutor(PreparedStatement pr, List<List<SqlParameter>> params) throws SQLException {
        this.prepareBatch(pr, params);

        pr.executeBatch();
        this.resultSet = pr.getGeneratedKeys();
    }

    private void prepareBatch(PreparedStatement pr, List<List<SqlParameter>> params) throws SQLException {
        for (List<SqlParameter> parameters : params) {
            this.prepare(pr, parameters);
            pr.addBatch();
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
    }
}
