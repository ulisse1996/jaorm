package io.github.ulisse1996.dsl.common;

import io.github.ulisse1996.entity.SqlColumn;

public interface EndSelect<T> extends EndJoin<T> {

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
