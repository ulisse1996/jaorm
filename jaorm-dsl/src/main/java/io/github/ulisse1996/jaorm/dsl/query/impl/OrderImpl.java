package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class OrderImpl {

    private final OrderType type;
    private final String alias;
    private final String column;
    private final SelectedImpl<?, ?> parent;

    public OrderImpl(OrderType type, SqlColumn<?,?> column, String alias, SelectedImpl<?, ?> parent) {
        this.type = type;
        this.alias = alias;
        this.column = column.getName();
        this.parent = parent;
    }

    public String asString() {
        return String.format(" %s.%s %s", fromOrAlias(), this.column, this.type);
    }

    private String fromOrAlias() {
        return this.alias != null ? this.alias : this.parent.getTable();
    }
}
