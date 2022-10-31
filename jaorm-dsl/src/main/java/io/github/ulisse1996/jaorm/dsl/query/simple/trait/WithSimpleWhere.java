package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.IntermediateSimpleWhere;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface WithSimpleWhere {

    <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column);
    <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column, String alias);
    <R> IntermediateSimpleWhere<R> where(VendorFunction<R> column);
    <R> IntermediateSimpleWhere<R> where(VendorFunction<R> column, String alias);
}
