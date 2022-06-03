package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedExecutable;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.util.Objects;

public class SetterImpl<T, R> implements IntermediateUpdate<T, R> {

    private final UpdatedImpl<T> parent;
    private final SqlColumn<T, R> column;
    private R value;
    private VendorFunction<R> function;

    SetterImpl(UpdatedImpl<T> parent, SqlColumn<T, R> column) {
        this.parent = parent;
        this.column = column;
    }

    @Override
    public UpdatedExecutable<T> toValue(R value) {
        this.value = Objects.requireNonNull(value, "Value can't be null !");
        return this.parent;
    }

    @Override
    public UpdatedExecutable<T> usingFunction(VendorFunction<R> function) {
        this.function = Objects.requireNonNull(function, "Function can't be null !");
        return this.parent;
    }

    public SqlColumn<T, R> getColumn() {
        return column;
    }

    public R getValue() {
        return value;
    }

    public VendorFunction<R> getFunction() {
        return function;
    }
}
