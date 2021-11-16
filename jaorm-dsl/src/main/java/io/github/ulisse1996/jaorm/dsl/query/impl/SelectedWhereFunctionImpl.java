package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class SelectedWhereFunctionImpl<T, R> extends WhereImpl<T, R> implements WhereFunctionImpl<T, R> {

    private final VendorFunction<R> function;

    public SelectedWhereFunctionImpl(VendorFunction<R> function, SelectedImpl<T, ?> parent, boolean or, String alias) {
        super(null, parent, or, alias);
        this.function = function;
    }

    @Override
    public VendorFunction<R> getFunction() {
        return function;
    }

    @Override
    public void assertIsString() {
        WhereFunctionImpl.super.assertIsString(function);
    }

    @Override
    public void buildClause(StringBuilder builder, boolean caseInsensitiveLike) {
        WhereFunctionImpl.super.buildClause(builder, caseInsensitiveLike, this);
    }
}
