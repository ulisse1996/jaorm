package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface IntermediateJoin<T> extends EndJoin<T> {

    <R> On<T, R> and(SqlColumn<T, R> column);
    <R> On<T, R> or(SqlColumn<T, R> column);
    <R> On<T, R> andOn(SqlColumn<T, R> column);
    <R> On<T, R> orOn(SqlColumn<T, R> column);
}
