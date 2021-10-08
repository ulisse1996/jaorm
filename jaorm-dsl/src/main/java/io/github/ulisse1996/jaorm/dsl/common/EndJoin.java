package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface EndJoin<T> extends Readable<T> {

    <L> Where<T, L> where(SqlColumn<T, L> column);
    <R, L> Where<T, L> whereJoinColumn(SqlColumn<R, L> column);
    <R, L> Where<T, L> whereJoinColumn(SqlColumn<R, L> column, String alias);

    Join<T> join(Class<?> table);
    Join<T> leftJoin(Class<?> table);
    Join<T> rightJoin(Class<?> table);
    Join<T> fullJoin(Class<?> table);

    Join<T> join(Class<?> table, String alias);
    Join<T> leftJoin(Class<?> table, String alias);
    Join<T> rightJoin(Class<?> table, String alias);
    Join<T> fullJoin(Class<?> table, String alias);

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
    Order<T> orderByJoinColumn(OrderType type, SqlColumn<?, ?> column);
    Order<T> orderByJoinColumn(OrderType type, SqlColumn<?, ?> column, String alias);
    Fetch<T> limit(int row);
    Offset<T> offset(int row);
}
