package io.github.ulisse1996.jaorm.dsl.impl;

import io.github.ulisse1996.jaorm.dsl.common.OrderType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderImpl<T> {

    private final String table;
    private final List<Sortable<T>> sortables;

    public OrderImpl(String table) {
        this.table = table;
        this.sortables = new ArrayList<>();
    }

    public void add(OrderType type, SqlColumn<T, ?> column) {
        this.sortables.add(new Sortable<>(type, column));
    }

    public String getSql() {
        return sortables.stream()
                .map(s -> this.table + "." + s.getSql())
                .collect(Collectors.joining(", "));
    }

    private static class Sortable<T> {

        private final SqlColumn<?,?> column;
        private final OrderType orderType;

        public Sortable(OrderType type, SqlColumn<T, ?> column) {
            this.orderType = type;
            this.column = column;
        }

        private String getSql() {
            return column.getName() + " " + orderType.name();
        }
    }
}
