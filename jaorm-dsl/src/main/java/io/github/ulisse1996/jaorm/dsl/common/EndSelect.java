package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface EndSelect<T> extends EndJoin<T> {

    Order<T> orderBy(OrderType type, SqlColumn<T, ?> column);
}
