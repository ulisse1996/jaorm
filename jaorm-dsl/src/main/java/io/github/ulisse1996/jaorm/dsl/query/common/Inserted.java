package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateInsert;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Inserted<T> {

    <R> IntermediateInsert<T, R> column(SqlColumn<T, R> column);
}
