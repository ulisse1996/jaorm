package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface SqlExecutor extends AutoCloseable {

    default void prepare(PreparedStatement pr, List<SqlParameter> parameters) throws SQLException {
        if (!parameters.isEmpty()) {
            for (int i = 0; i < parameters.size(); i++) {
                int index = i + 1;
                SqlParameter parameter = parameters.get(i);
                parameter.getAccessor().set(pr, index, parameter.getVal());
            }
        }
    }
}
