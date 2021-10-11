package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class WhereFunctionImpl<T, R> extends WhereImpl<T, R> {

    private final VendorFunction<R> function;

    public WhereFunctionImpl(VendorFunction<R> function, SelectedImpl<T, ?> parent, boolean or, String alias) {
        super(null, parent, or, alias);
        this.function = function;
    }

    @Override
    protected void buildClause(StringBuilder builder, boolean caseInsensitiveLike) {
        String format = function.apply(getFrom(this));
        builder.append(format).append(evaluateOperation(this, caseInsensitiveLike));
        buildLinked(builder, caseInsensitiveLike);
    }

    @Override
    protected void buildLinked(StringBuilder builder, boolean caseInsensitiveLike) {
        if (!this.links.isEmpty()) {
            for (WhereImpl<?, ?> inner : this.links) {
                if (inner instanceof WhereFunctionImpl<?, ?>) {
                    String format = function.apply(getFrom(inner));
                    builder.append(inner.or ? OR_CLAUSE : AND_CLAUSE)
                            .append(format).append(evaluateOperation(inner, caseInsensitiveLike));
                } else {
                    buildInner(builder, caseInsensitiveLike, inner);
                }
            }
        }
    }

    @Override
    protected void assertIsString() {
        if (!this.function.isString()) {
            throw new IllegalArgumentException("Can't use like without a column that match String.class");
        }
    }
}
