package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.spi.CacheService;

import java.util.Collections;
import java.util.Map;

public class NoOpCache extends CacheService {

    public static final NoOpCache INSTANCE = new NoOpCache();

    @Override
    public void setConfiguration(Class<?> klass, AbstractCacheConfiguration configuration) {
        // Nothing here
    }

    @Override
    public <T> JaormCache<T> getCache(Class<T> klass) {
        return null;
    }

    @Override
    public <T> JaormAllCache<T> getCacheAll(Class<T> klass) {
        return null;
    }

    @Override
    public <T> boolean isCacheable(Class<T> klass) {
        return false;
    }

    @Override
    public Map<Class<?>, EntityCache<?>> getCaches() {
        return Collections.emptyMap();
    }
}
