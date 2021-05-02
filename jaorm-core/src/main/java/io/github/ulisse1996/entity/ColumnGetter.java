package io.github.ulisse1996.entity;

import java.util.function.Function;

public interface ColumnGetter<T, R> extends Function<T, R> {

    R apply(T entity);
}
