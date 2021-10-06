package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface EndJoin<T> extends Readable<T> {

    <L> Where<T, L> where(SqlColumn<T, L> column);
    <R, L> Where<T, L> whereJoinColumn(SqlColumn<R, L> column);

    Join<T> join(Class<?> table);
    Join<T> leftJoin(Class<?> table);
    Join<T> rightJoin(Class<?> table);
    Join<T> fullJoin(Class<?> table);
}
