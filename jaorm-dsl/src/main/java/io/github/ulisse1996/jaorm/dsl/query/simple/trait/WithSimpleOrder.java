package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleOrder;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface WithSimpleOrder {

    SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column, String alias);
    SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column);
}
