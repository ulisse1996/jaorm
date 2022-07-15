package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class UpdateExecutor implements SqlExecutor {

    private final ResultSet resultSet;
    private final int updateRow;

    public UpdateExecutor(PreparedStatement pr, List<SqlParameter> parameters, Set<String> autoGenKeys) throws SQLException {
        this.prepare(pr, parameters);

        this.updateRow = pr.executeUpdate();

        if (autoGenKeys != null && !autoGenKeys.isEmpty()) {
            this.resultSet = pr.getGeneratedKeys();
        } else {
            this.resultSet = null;
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public int getUpdateRow() {
        return updateRow;
    }

    @Override
    public void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
    }
}
