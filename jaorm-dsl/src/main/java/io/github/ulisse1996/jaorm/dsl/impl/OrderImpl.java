package io.github.ulisse1996.jaorm.dsl.impl;

import io.github.ulisse1996.jaorm.dsl.common.OrderType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderImpl {

    private final List<Sortable> sortables;

    public OrderImpl() {
        this.sortables = new ArrayList<>();
    }

    public void add(OrderType type, SqlColumn<?, ?> column, String table) {
        this.sortables.add(new Sortable(type, column, table));
    }

    public String getSql() {
        return sortables.stream()
                .map(Sortable::getSql)
                .collect(Collectors.joining(", "));
    }

    private static class Sortable {

        private final String table;
        private final SqlColumn<?,?> column;
        private final OrderType orderType;

        public Sortable(OrderType type, SqlColumn<?, ?> column, String table) {
            this.orderType = type;
            this.column = column;
            this.table = table;
        }

        private String getSql() {
            return this.table + "." + column.getName() + " " + orderType.name();
        }
    }
}
