package io.github.ulisse1996.jaorm.entity.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlSetter<R> {

    void set(PreparedStatement pr, int index, R value) throws SQLException;
}
