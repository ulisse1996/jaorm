package io.github.ulisse1996.jaorm.dsl.common;

import java.util.List;
import java.util.Optional;

public interface Readable<T> {

    Readable<T> getParent();

    default T read() {
        return getParent().read();
    }

    default Optional<T> readOpt() {
        return getParent().readOpt();
    }

    default List<T> readAll() {
        return getParent().readAll();
    }

    default long count() {
        return getParent().count();
    }
}
