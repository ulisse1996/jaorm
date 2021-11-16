package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class UpdatedWhereFunctionImpl<T, R> extends UpdatedWhereImpl<T, R> implements WhereFunctionImpl<R> {

    private final VendorFunction<R> function;

    public UpdatedWhereFunctionImpl(VendorFunction<R> function, UpdatedImpl<T> parent, boolean or) {
        super(null, parent, or);
        this.function = function;
    }

    @Override
    public VendorFunction<R> getFunction() {
        return function;
    }
}
