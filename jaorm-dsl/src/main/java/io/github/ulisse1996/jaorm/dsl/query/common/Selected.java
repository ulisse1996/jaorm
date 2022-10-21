package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.*;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface Selected<T> extends WithResult<T>, WithJoin<T>, WithOrder<T>, WithLimit<T>, WithOffset<T>, WithPage<T>, WithCount {

    <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column);
    <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column, String alias);
    <R> IntermediateWhere<T, R> where(VendorFunction<R> column);
    <R> IntermediateWhere<T, R> where(VendorFunction<R> column, String alias);
}
