package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

import java.util.List;
import java.util.Optional;

public interface EndSelect<T> {

    <L> Where<T, L> where(SqlColumn<T, L> column);
    <L> Where<T, L> orWhere(SqlColumn<T, L> column);

    T read();
    Optional<T> readOpt();
    List<T> readAll();
}
