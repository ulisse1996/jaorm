package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.MergeUsing;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface Merge<T> {

    <R> MergeUsing<T> using(SqlColumn<T, R> column, R value);
    <R> MergeUsing<T> using(SqlColumn<T, R> column, VendorFunction<R> function);
}
