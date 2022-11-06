package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.spi.CacheService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class CustomCacheInstance extends CacheService {

    @Override
    public <T> boolean isCacheable(Class<T> klass) {
        return false;
    }

    @Override
    public Map<Class<?>, EntityCache<?>> getCaches() {
        return Collections.emptyMap();
    }
}
