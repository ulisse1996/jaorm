package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface Order<T> extends Readable<T> {

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
