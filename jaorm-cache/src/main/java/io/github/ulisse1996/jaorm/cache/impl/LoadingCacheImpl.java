package io.github.ulisse1996.jaorm.cache.impl;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.cache.JaormCache;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;

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
