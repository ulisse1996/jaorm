package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;

public interface IntermediateWhere<T, R> {

    // El Expression operations
    SelectedWhere<T> eq(R val);
    SelectedWhere<T> ne(R val);
    SelectedWhere<T> lt(R val);
    SelectedWhere<T> gt(R val);
    SelectedWhere<T> le(R val);
    SelectedWhere<T> ge(R val);

    // Standard operations
    SelectedWhere<T> equalsTo(R val);
    SelectedWhere<T> notEqualsTo(R val);
    SelectedWhere<T> lessThan(R val);
    SelectedWhere<T> greaterThan(R val);
    SelectedWhere<T> lessOrEqualsTo(R val);
    SelectedWhere<T> greaterOrEqualsTo(R val);

    // Other operations
    SelectedWhere<T> in(Iterable<R> iterable);
    SelectedWhere<T> notIn(Iterable<R> iterable);
    SelectedWhere<T> isNull();
    SelectedWhere<T> isNotNull();
    SelectedWhere<T> like(LikeType type, String val);
    SelectedWhere<T> notLike(LikeType type, String val);
}
