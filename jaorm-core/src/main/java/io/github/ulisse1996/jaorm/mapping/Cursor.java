package io.github.ulisse1996.jaorm.mapping;

public interface Cursor<T> extends AutoCloseable, Iterable<T> {

    boolean isFetched();
}
