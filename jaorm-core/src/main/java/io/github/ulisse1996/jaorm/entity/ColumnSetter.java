package io.github.ulisse1996.jaorm.entity;

import java.util.function.BiConsumer;

public interface ColumnSetter<T, R> extends BiConsumer<T, R> {

    void accept(T entity, R value);
}
