package io.github.ulisse1996.dsl.common;

public interface Operation<R, X> {

    // El Expression operations
    X eq(R val);
    X ne(R val);
    X lt(R val);
    X gt(R val);
    X le(R val);
    X ge(R val);

    // Standard operations
    X equalsTo(R val);
    X notEqualsTo(R val);
    X lessThan(R val);
    X greaterThan(R val);
    X lessOrEqualsTo(R val);
    X greaterOrEqualsTo(R val);

    // Other operations
    X in(Iterable<R> iterable);
    X notIn(Iterable<R> iterable);
    X isNull();
    X isNotNull();
    X like(LikeType type, String val);
    X notLike(LikeType type, String val);

}
