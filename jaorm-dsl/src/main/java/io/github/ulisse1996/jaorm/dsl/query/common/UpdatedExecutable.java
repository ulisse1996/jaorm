package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdatedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithExecute;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface UpdatedExecutable<T> extends Updated<T>, WithExecute {

    <R> IntermediateUpdatedWhere<T, R> where(SqlColumn<?, R> column);
    <R> IntermediateUpdatedWhere<T, R> where(VendorFunction<R> column);
}
