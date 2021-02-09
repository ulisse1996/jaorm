package io.jaorm.cache;

import io.jaorm.Arguments;

import java.util.Optional;

public interface JaormCache<T> {

    T get(Arguments arguments);
    Optional<T> getOpt(Arguments arguments);
}
