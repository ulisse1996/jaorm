package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface MergeUsing<T> {

    <R> MergeUsing<T> using(SqlColumn<T, R> column, R value);
    <R> MergeUsing<T> using(SqlColumn<T, R> column, VendorFunction<R> function);

    MergedOn<T> onEquals(SqlColumn<T, ?> column);
}
