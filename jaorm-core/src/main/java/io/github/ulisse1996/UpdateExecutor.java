package io.github.ulisse1996;

import io.github.ulisse1996.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UpdateExecutor implements SqlExecutor {

    private final ResultSet resultSet;

    public UpdateExecutor(PreparedStatement pr, List<SqlParameter> parameters) throws SQLException {
        this.prepare(pr, parameters);

        pr.executeUpdate();
        this.resultSet = pr.getGeneratedKeys();
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
