package io.jaorm;

import io.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ResultSetExecutor implements AutoCloseable {

    private final ResultSet resultSet;

    public ResultSetExecutor(PreparedStatement pr, List<SqlParameter> parameters) throws SQLException {
        if (!parameters.isEmpty()) {
            for (int i = 0; i < parameters.size(); i++) {
                int index = i + 1;
                SqlParameter parameter = parameters.get(i);
                parameter.getAccessor().set(pr, index, parameter.getVal());
            }
        }

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
