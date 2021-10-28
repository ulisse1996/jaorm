package io.github.ulisse1996.jaorm.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ProjectionDelegate {

    void setEntity(ResultSet resultSet) throws SQLException;
}
