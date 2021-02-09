package io.jaorm.cache;

import io.jaorm.Arguments;
import io.jaorm.ServiceFinder;

import java.util.*;

public abstract class CacheService {

    private static CacheService instance;

    public static synchronized CacheService getCurrent() {
        try {
            if (instance == null) {
                instance = ServiceFinder.loadService(CacheService.class);
            }
        } catch (IllegalArgumentException | ServiceConfigurationError ex) {
            instance = NoCache.INSTANCE;
        }

        return instance;
    }

    public abstract <T> boolean isCacheable(Class<T> klass);
    public abstract Map<Class<?>, EntityCache<?>> getCaches(); // NOSONAR

    @SuppressWarnings("unchecked")
    protected <T> JaormCache<T> getCache(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().get(klass);
        return (JaormCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getCache();
    }

    @SuppressWarnings("unchecked")
    protected  <T> JaormAllCache<T> getCacheAll(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().get(klass);
        return (JaormAllCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getAllCache();
    }

    public void setConfiguration(Class<?> klass, AbstractCacheConfiguration configuration) {
        getCaches().put(klass, EntityCache.fromConfiguration(klass, configuration));
    }

    public boolean isCacheActive() {
        return false; // Default implementation return true
    }

    public  <T> T get(Class<T> klass, Arguments arguments) {
        return getCache(klass).get(arguments);
    }

    public  <T> List<T> getAll(Class<T> klass) {
        return getCacheAll(klass).getAll();
    }

    public  <T> Optional<T> getOpt(Class<T> klass, Arguments arguments) {
        return getCache(klass).getOpt(arguments);
    }
}
