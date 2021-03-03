package io.jaorm.dsl.common;

import io.jaorm.dsl.impl.LikeType;

public interface WhereOperation<T, R> {

    // El Expression operations
    IntermediateWhere<T> eq(R val);
    IntermediateWhere<T> ne(R val);
    IntermediateWhere<T> lt(R val);
    IntermediateWhere<T> gt(R val);
    IntermediateWhere<T> le(R val);
    IntermediateWhere<T> ge(R val);

    // Standard operations
    IntermediateWhere<T> equalsTo(R val);
    IntermediateWhere<T> notEqualsTo(R val);
    IntermediateWhere<T> lessThan(R val);
    IntermediateWhere<T> greaterThan(R val);
    IntermediateWhere<T> lessOrEqualsTo(R val);
    IntermediateWhere<T> greaterOrEqualsTo(R val);

    IntermediateWhere<T> in(Iterable<R> iterable);
    IntermediateWhere<T> notIn(Iterable<R> iterable);
    IntermediateWhere<T> isNull();
    IntermediateWhere<T> isNotNull();
    IntermediateWhere<T> like(LikeType type, String val);
    IntermediateWhere<T> notLike(LikeType type, String val);

}
