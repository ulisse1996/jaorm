package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class SelectedWhereFunctionImpl<T, R> extends WhereImpl<T, R> implements WhereFunctionImpl<T, R> {

    private final VendorFunction<R> function;

    public SelectedWhereFunctionImpl(VendorFunction<R> function, SelectedImpl<T, ?> parent, boolean or, String alias) {
        super(null, parent, or, alias);
        this.function = function;
    }

    @Override
    protected void buildClause(StringBuilder builder, boolean caseInsensitiveLike) {
        this.buildClause(links, function, builder, caseInsensitiveLike, this);
    }

    @Override
    public String evaluateOperation(AbstractWhereImpl<?, ?> inner, boolean caseInsensitiveLike) {
        return super.evaluateOperation(inner, caseInsensitiveLike);
    }

    @Override
    public String getFrom(AbstractWhereImpl<?, ?> where) {
        return super.getFrom(where);
    }

    @Override
    public void assertIsString() {
        WhereFunctionImpl.super.assertIsString(function);
    }

    @Override
    public void buildInner(StringBuilder builder, boolean caseInsensitiveLike, AbstractWhereImpl<?, ?> inner) {
        super.buildInner(builder, caseInsensitiveLike, inner);
    }
}
