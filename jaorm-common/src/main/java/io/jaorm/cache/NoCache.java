package io.jaorm.cache;

import java.util.Collections;
import java.util.Map;

public class NoCache extends CacheService {

    public static final NoCache INSTANCE = new NoCache();

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
