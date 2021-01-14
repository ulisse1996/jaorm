package io.jaorm.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public interface EntityDelegate<T> {

    Supplier<T> getEntityInstance();
    EntityMapper<T> getEntityMapper();
    void setEntity(ResultSet rs) throws SQLException;
    default T toEntity(ResultSet rs) throws SQLException {
        return getEntityMapper().map(getEntityInstance(), rs);
    }
}
