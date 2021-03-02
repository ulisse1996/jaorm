package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface IntermediateWhere<T> extends EndSelect<T> {

    <L> Where<T, L> and(SqlColumn<T, L> column);
    <L> Where<T, L> or(SqlColumn<T, L> column);
}
