package io.github.ulisse1996.jaorm.dsl.common;

import java.util.List;
import java.util.Optional;

public interface Readable<T> {

    T read();
    Optional<T> readOpt();
    List<T> readAll();
    long count();
}
