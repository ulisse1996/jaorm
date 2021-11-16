package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdatedWhere;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface WithUpdatedWhere<T> {

    <R> IntermediateUpdatedWhere<T, R> andWhere(SqlColumn<?, R> column);
    <R> IntermediateUpdatedWhere<T, R> orWhere(SqlColumn<?, R> column);
    <R> IntermediateUpdatedWhere<T, R> and(SqlColumn<?, R> column);
    <R> IntermediateUpdatedWhere<T, R> or(SqlColumn<?, R> column);

    <R> IntermediateUpdatedWhere<T, R> andWhere(VendorFunction<R> function);
    <R> IntermediateUpdatedWhere<T, R> orWhere(VendorFunction<R> function);
    <R> IntermediateUpdatedWhere<T, R> and(VendorFunction<R> function);
    <R> IntermediateUpdatedWhere<T, R> or(VendorFunction<R> function);
}
