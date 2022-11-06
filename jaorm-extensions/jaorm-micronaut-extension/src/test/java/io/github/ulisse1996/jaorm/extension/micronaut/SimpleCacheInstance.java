package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.spi.CacheService;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.Map;

@Singleton
public class SimpleCacheInstance extends CacheService {

    @Override
    public <T> boolean isCacheable(Class<T> klass) {
        return false;
    }

    @Override
    public Map<Class<?>, EntityCache<?>> getCaches() {
        return Collections.emptyMap();
    }
}
