package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface WhereFunctionImpl<R> {

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
