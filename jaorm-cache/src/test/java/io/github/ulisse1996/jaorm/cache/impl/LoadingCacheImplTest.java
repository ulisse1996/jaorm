package io.github.ulisse1996.jaorm.cache.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

class LoadingCacheImplTest {

    @Test
    void should_return_cached_argument() {
        LoadingCache<Arguments, String> cache = Caffeine.newBuilder()
                .build(arguments -> {
                    Integer val = (Integer) arguments.getValues()[0];
                    return String.valueOf(val + 1);
                });
        LoadingCacheImpl<String> impl = new LoadingCacheImpl<>(cache);
        Assertions.assertEquals("2", impl.get(Arguments.of(1)));
    }

    @Test
    void should_return_opt_value() {
        LoadingCache<Arguments, String> cache = Caffeine.newBuilder()
                .build(arguments -> {
                    Integer val = (Integer) arguments.getValues()[0];
                    return String.valueOf(val + 1);
                });
        LoadingCacheImpl<String> impl = new LoadingCacheImpl<>(cache);
        Optional<String> opt = impl.getOpt(Arguments.of(1));
        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("2", opt.get());
    }

    @Test
    void should_return_optional_value() {
        LoadingCache<Arguments, String> cache = Caffeine.newBuilder()
                .build(arguments -> {
                    Integer val = (Integer) arguments.getValues()[0];
                    return String.valueOf(val + 1);
                });
        LoadingCacheImpl<String> impl = new LoadingCacheImpl<>(cache);
        Optional<String> opt = impl.getOpt(Arguments.of(1));
        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("2", opt.get());
    }

    @Test
    void should_return_empty_optional() {
        LoadingCache<Arguments, String> cache = Caffeine.newBuilder()
                .build(arguments -> {
                    throw new JaormSqlException(new SQLException());
                });
        LoadingCacheImpl<String> impl = new LoadingCacheImpl<>(cache);
        Optional<String> opt = impl.getOpt(Arguments.of(1));
        Assertions.assertFalse(opt.isPresent());
    }
}
