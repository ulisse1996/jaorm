package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.schema.TableInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ProjectionDelegate {

    ProjectionDelegate getInstance();
    Class<?> getProjectionClass();
    TableInfo asTableInfo();
    void setEntity(ResultSet resultSet) throws SQLException;
}
