package io.jaorm.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<R> {

    R map(ResultSet resultSet) throws SQLException;
}
