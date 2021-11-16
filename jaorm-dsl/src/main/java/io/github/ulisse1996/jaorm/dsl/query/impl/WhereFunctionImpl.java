package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.util.List;

public interface WhereFunctionImpl<T, R> {
    String AND_CLAUSE = " AND ";
    String OR_CLAUSE = " OR ";

    default void buildClause(StringBuilder builder, boolean caseInsensitiveLike, AbstractWhereImpl<T, R> where) {
        String format = getFormat(getFunction(), caseInsensitiveLike, where);
        builder.append(format).append(where.evaluateOperation(where, caseInsensitiveLike));
        buildLinked(where.links, getFunction(), builder, caseInsensitiveLike);
    }

    default void buildLinked(List<AbstractWhereImpl<T, ?>> links, VendorFunction<?> function, StringBuilder builder, boolean caseInsensitiveLike) {
        if (!links.isEmpty()) {
            for (AbstractWhereImpl<?, ?> inner : links) {
                if (inner instanceof SelectedWhereFunctionImpl<?, ?>) {
                    String format = getFormat(function, caseInsensitiveLike, inner);
                    builder.append(inner.or ? OR_CLAUSE : AND_CLAUSE)
                            .append(format).append(inner.evaluateOperation(inner, caseInsensitiveLike));
                } else {
                    inner.buildInner(builder, caseInsensitiveLike, inner);
                }
            }
        }
    }

    default String getFormat(VendorFunction<?> function, boolean caseInsensitiveLike, AbstractWhereImpl<?, ?> where) {
        String format = function.apply(where.getFrom(where));
        if (caseInsensitiveLike && where.likeType != null) {
            format = String.format("UPPER(%s)", format);
        }
        return format;
    }

    default void assertIsString(VendorFunction<?> function) {
        if (!function.isString()) {
            throw new IllegalArgumentException("Can't use like without a column that match String.class");
        }
    }

    VendorFunction<R> getFunction();
}
