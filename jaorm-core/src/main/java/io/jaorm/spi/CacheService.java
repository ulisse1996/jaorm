package io.jaorm.spi;

import io.jaorm.Arguments;
import io.jaorm.ServiceFinder;
import io.jaorm.cache.*;
import io.jaorm.logger.JaormLogger;
import io.jaorm.spi.common.Singleton;

import java.util.*;

public abstract class CacheService {

    private static final JaormLogger logger = JaormLogger.getLogger(CacheService.class);
    private static final Singleton<CacheService> INSTANCE = Singleton.instance();

    public static synchronized CacheService getCurrent() {
        try {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(ServiceFinder.loadService(CacheService.class));
            }
        } catch (IllegalArgumentException | ServiceConfigurationError ex) {
            logger.debug(() -> "Can't find cache service, switching to NoCache instance");
            INSTANCE.set(NoCache.INSTANCE);
        }

        return INSTANCE.get();
    }

    public abstract <T> boolean isCacheable(Class<T> klass);
    public abstract Map<Class<?>, EntityCache<?>> getCaches(); // NOSONAR

    @SuppressWarnings("unchecked")
    public <T> JaormCache<T> getCache(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().get(klass);
        return (JaormCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getCache();
    }

    @SuppressWarnings("unchecked")
    public <T> JaormAllCache<T> getCacheAll(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().get(klass);
        return (JaormAllCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getAllCache();
    }

    public void setConfiguration(Class<?> klass, AbstractCacheConfiguration configuration) {
        logger.debug(() -> "Setting cache configuration for " + klass);
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
