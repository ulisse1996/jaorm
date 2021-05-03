package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Order<T> extends Readable<T> {

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
