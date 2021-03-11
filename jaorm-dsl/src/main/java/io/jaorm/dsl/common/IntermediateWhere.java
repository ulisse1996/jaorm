package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface IntermediateWhere<T> extends Readable<T> {

    <L> Where<T, L> where(SqlColumn<T, L> column);
    <L> Where<T, L> orWhere(SqlColumn<T, L> column);
    <L> Where<T, L> and(SqlColumn<T, L> column);
    <L> Where<T, L> or(SqlColumn<T, L> column);
    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
