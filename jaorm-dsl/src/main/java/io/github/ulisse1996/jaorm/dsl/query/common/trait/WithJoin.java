package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.On;

public interface WithJoin<T> {

    <R> On<T, R> join(Class<R> table);
    <R> On<T, R> leftJoin(Class<R> table);
    <R> On<T, R> rightJoin(Class<R> table);
    <R> On<T, R> fullJoin(Class<R> table);

    <R> On<T, R> join(Class<R> table, String alias);
    <R> On<T, R> leftJoin(Class<R> table, String alias);
    <R> On<T, R> rightJoin(Class<R> table, String alias);
    <R> On<T, R> fullJoin(Class<R> table, String alias);
}
