package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.InsertedExecutable;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateInsert;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.Objects;

public class InsertingImpl<T, R> implements IntermediateInsert<T, R> {

    private final InsertedExecutable<T> parent;
    private final SqlColumn<T, R> column;
    private R value;

    InsertingImpl(InsertedExecutable<T> parent, SqlColumn<T, R> column) {
        this.parent = parent;
        this.column = column;
    }

    @Override
    public InsertedExecutable<T> withValue(R value) {
        this.value = Objects.requireNonNull(value, "Value can't be null !");
        return this.parent;
    }

    public SqlColumn<T, R> getColumn() {
        return column;
    }

    public R getValue() {
        return value;
    }
}
