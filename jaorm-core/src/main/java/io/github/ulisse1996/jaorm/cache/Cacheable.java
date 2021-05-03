package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.spi.CacheService;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Cacheable {

    private Cacheable() {}

    public static <T> T getCached(Class<T> klass, Arguments arguments, Supplier<T> executor) {
        CacheService cacheService = CacheService.getInstance();
        if (cacheService.isCacheActive() && cacheService.isCacheable(klass)) {
            return cacheService.get(klass, arguments);
        }

        return executor.get();
    }

    public static <T> Optional<T> getCachedOpt(Class<T> klass, Arguments arguments, Supplier<Optional<T>> executor) {
        CacheService cacheService = CacheService.getInstance();
        if (cacheService.isCacheActive() && cacheService.isCacheable(klass)) {
            return cacheService.getOpt(klass, arguments);
        }

        return executor.get();
    }

    public static <T> List<T> getCachedAll(Class<T> klass, Supplier<List<T>> executor) {
        CacheService cacheService = CacheService.getInstance();
        if (cacheService.isCacheActive() && cacheService.isCacheable(klass)) {
            return cacheService.getAll(klass);
        }

        return executor.get();
    }
}
