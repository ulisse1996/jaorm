package io.jaorm.dsl.common;

import io.jaorm.dsl.impl.LikeType;

public interface WhereOperation<T> {

    IntermediateWhere<T> eq(Object val);
    IntermediateWhere<T> ne(Object val);
    IntermediateWhere<T> lt(Object val);
    IntermediateWhere<T> gt(Object val);
    IntermediateWhere<T> le(Object val);
    IntermediateWhere<T> ge(Object val);
    IntermediateWhere<T> in(Iterable<?> iterable);
    IntermediateWhere<T> notIn(Iterable<?> iterable);
    IntermediateWhere<T> isNull();
    IntermediateWhere<T> isNotNull();
    IntermediateWhere<T> like(LikeType type, String val);
}
