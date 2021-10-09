package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedOrder;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface WithOrder<T> {

    SelectedOrder<T> orderBy(OrderType type, SqlColumn<?, ?> column, String alias);
    SelectedOrder<T> orderBy(OrderType type, SqlColumn<?, ?> column);
}
