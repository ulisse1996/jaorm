package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Order<T> extends Readable<T> {

    Fetch<T> limit(int row);
    Offset<T> offset(int row);
    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
    Order<T> orderByJoinColumn(OrderType type, SqlColumn<?, ?> column);
    Order<T> orderByJoinColumn(OrderType type, SqlColumn<?, ?> column, String alias);
}
