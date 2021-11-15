package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class UpdatedWhereFunctionImpl<T, R> extends UpdatedWhereImpl<T, R> {

    private final VendorFunction<R> function;

    public UpdatedWhereFunctionImpl(VendorFunction<R> function, UpdatedImpl<T> parent, boolean or) {
        super(null, parent, or);
        this.function = function;
    }

    @Override
    protected void buildClause(StringBuilder builder, boolean caseInsensitiveLike) {
        String format = getFormat(caseInsensitiveLike, this);
        builder.append(format).append(evaluateOperation(this, caseInsensitiveLike));
        buildLinked(builder, caseInsensitiveLike);
    }

    @Override
    protected void buildLinked(StringBuilder builder, boolean caseInsensitiveLike) {
        if (!this.links.isEmpty()) {
            for (AbstractWhereImpl<?, ?> inner : this.links) {
                if (inner instanceof UpdatedWhereFunctionImpl<?, ?>) {
                    String format = getFormat(caseInsensitiveLike, inner);
                    builder.append(inner.or ? OR_CLAUSE : AND_CLAUSE)
                            .append(format).append(evaluateOperation(inner, caseInsensitiveLike));
                } else {
                    buildInner(builder, caseInsensitiveLike, inner);
                }
            }
        }
    }

    private String getFormat(boolean caseInsensitiveLike, AbstractWhereImpl<?, ?> where) {
        String format = function.apply(getFrom(where));
        if (caseInsensitiveLike && where.likeType != null) {
            format = String.format("UPPER(%s)", format);
        }
        return format;
    }

    @Override
    protected void assertIsString() {
        if (!this.function.isString()) {
            throw new IllegalArgumentException("Can't use like without a column that match String.class");
        }
    }
}
