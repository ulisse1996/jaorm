package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.Arguments;

import java.util.Optional;

public interface JaormCache<T> {

    T get(Arguments arguments);
    Optional<T> getOpt(Arguments arguments);
}
