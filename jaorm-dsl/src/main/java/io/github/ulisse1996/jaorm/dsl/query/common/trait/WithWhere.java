package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface WithWhere<T> {

    <R> IntermediateWhere<T, R> andWhere(SqlColumn<?, R> column);
    <R> IntermediateWhere<T, R> orWhere(SqlColumn<?, R> column);
    <R> IntermediateWhere<T, R> and(SqlColumn<?, R> column);
    <R> IntermediateWhere<T, R> or(SqlColumn<?, R> column);

    <R> IntermediateWhere<T, R> andWhere(SqlColumn<?, R> column, String alias);
    <R> IntermediateWhere<T, R> orWhere(SqlColumn<?, R> column, String alias);
    <R> IntermediateWhere<T, R> and(SqlColumn<?, R> column, String alias);
    <R> IntermediateWhere<T, R> or(SqlColumn<?, R> column, String alias);

    <R> IntermediateWhere<T, R> andWhere(VendorFunction<R> function);
    <R> IntermediateWhere<T, R> orWhere(VendorFunction<R> function);
    <R> IntermediateWhere<T, R> and(VendorFunction<R> function);
    <R> IntermediateWhere<T, R> or(VendorFunction<R> function);

    <R> IntermediateWhere<T, R> andWhere(VendorFunction<R> function, String alias);
    <R> IntermediateWhere<T, R> orWhere(VendorFunction<R> function, String alias);
    <R> IntermediateWhere<T, R> and(VendorFunction<R> function, String alias);
    <R> IntermediateWhere<T, R> or(VendorFunction<R> function, String alias);
}
