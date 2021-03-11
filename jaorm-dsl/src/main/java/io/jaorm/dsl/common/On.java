package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface On<T, R> extends SimpleOnOperation<T, R> {

    // El Expression operations
    <L> IntermediateJoin<T> eq(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> ne(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> lt(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> gt(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> le(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> ge(SqlColumn<L, R> column);

    // Standard operations
    <L> IntermediateJoin<T> equalsTo(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> notEqualsTo(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> lessThan(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> greaterThan(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> lessOrEqualsTo(SqlColumn<L, R> column);
    <L> IntermediateJoin<T> greaterOrEqualsTo(SqlColumn<L, R> column);

    // Other operations
    <L> IntermediateJoin<T> inColumns(Iterable<SqlColumn<L, R>> iterable);
    <L> IntermediateJoin<T> notInColumns(Iterable<SqlColumn<L, R>> iterable);
    <L> IntermediateJoin<T> like(LikeType type, SqlColumn<L, String> val);
    <L> IntermediateJoin<T> notLike(LikeType type, SqlColumn<L, String> val);
}
