package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ResultSetExecutor implements SqlExecutor {

    private final ResultSet resultSet;

    public ResultSetExecutor(PreparedStatement pr, List<SqlParameter> parameters) throws SQLException {
        this.prepare(pr, parameters);
        this.resultSet = pr.executeQuery();
    }

    @Override
    public void close() throws SQLException {
        if (this.resultSet != null) {
            resultSet.close();
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
}
