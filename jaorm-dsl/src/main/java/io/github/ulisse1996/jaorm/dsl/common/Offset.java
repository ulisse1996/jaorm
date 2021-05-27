package io.github.ulisse1996.jaorm.dsl.common;

public interface Offset<T> {

    Fetch<T> limit(int row);
}
