package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.IntermediateSimpleWhere;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface WithAndOrWhere {

    <R> IntermediateSimpleWhere<R> andWhere(SqlColumn<?, R> column);
    <R> IntermediateSimpleWhere<R> orWhere(SqlColumn<?, R> column);
    <R> IntermediateSimpleWhere<R> and(SqlColumn<?, R> column);
    <R> IntermediateSimpleWhere<R> or(SqlColumn<?, R> column);

    <R> IntermediateSimpleWhere<R> andWhere(SqlColumn<?, R> column, String alias);
    <R> IntermediateSimpleWhere<R> orWhere(SqlColumn<?, R> column, String alias);
    <R> IntermediateSimpleWhere<R> and(SqlColumn<?, R> column, String alias);
    <R> IntermediateSimpleWhere<R> or(SqlColumn<?, R> column, String alias);

    <R> IntermediateSimpleWhere<R> andWhere(VendorFunction<R> function);
    <R> IntermediateSimpleWhere<R> orWhere(VendorFunction<R> function);
    <R> IntermediateSimpleWhere<R> and(VendorFunction<R> function);
    <R> IntermediateSimpleWhere<R> or(VendorFunction<R> function);

    <R> IntermediateSimpleWhere<R> andWhere(VendorFunction<R> function, String alias);
    <R> IntermediateSimpleWhere<R> orWhere(VendorFunction<R> function, String alias);
    <R> IntermediateSimpleWhere<R> and(VendorFunction<R> function, String alias);
    <R> IntermediateSimpleWhere<R> or(VendorFunction<R> function, String alias);
}
