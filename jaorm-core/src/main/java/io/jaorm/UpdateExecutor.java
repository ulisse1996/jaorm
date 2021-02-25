package io.jaorm;

import io.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UpdateExecutor implements SqlExecutor {

    public UpdateExecutor(PreparedStatement pr, List<SqlParameter> parameters) throws SQLException {
        this.prepare(pr, parameters);

        pr.executeUpdate();
    }
}
