package io.github.ulisse1996.dsl.common;

import io.github.ulisse1996.entity.SqlColumn;

public interface Order<T> extends Readable<T> {

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
