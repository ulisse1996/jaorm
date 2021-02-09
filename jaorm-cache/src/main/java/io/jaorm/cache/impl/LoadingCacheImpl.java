package io.jaorm.cache.impl;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.jaorm.Arguments;
import io.jaorm.cache.JaormCache;
import io.jaorm.exception.JaormSqlException;

import java.util.Optional;

public class LoadingCacheImpl<T> implements JaormCache<T> {

    private final LoadingCache<Arguments, T> cache;

    public LoadingCacheImpl(LoadingCache<Arguments, T> cache) {
        this.cache = cache;
    }

    @Override
    public T get(Arguments arguments) {
        return cache.get(arguments);
    }

    @Override
    public Optional<T> getOpt(Arguments arguments) {
        try {
            return Optional.ofNullable(get(arguments));
        } catch (JaormSqlException ex) {
            return Optional.empty();
        }
    }
}
