package io.github.ulisse1996.spi;

import io.github.ulisse1996.Arguments;
import io.github.ulisse1996.ServiceFinder;
import io.github.ulisse1996.cache.*;
import io.github.ulisse1996.cache.*;
import io.github.ulisse1996.logger.JaormLogger;
import io.github.ulisse1996.spi.common.Singleton;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class CacheService {

    private static final JaormLogger logger = JaormLogger.getLogger(CacheService.class);
    private static final Singleton<CacheService> INSTANCE = Singleton.instance();

    public static synchronized CacheService getInstance() {
        try {
            if (!INSTANCE.isPresent()) {
                Iterable<CacheService> cacheServices = ServiceFinder.loadServices(CacheService.class);
                List<CacheService> services = StreamSupport.stream(cacheServices.spliterator(), false)
                        .collect(Collectors.toList());

                if (services.size() == 1) {
                    INSTANCE.set(services.get(0));
                } else {
                    INSTANCE.set(
                            services.stream()
                                .filter(cacheService -> !cacheService.isDefault())
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Custom implementation must not override isDefault or must return false for isDefault check"))
                    );
                }
            }
        } catch (IllegalArgumentException | ServiceConfigurationError ex) {
            logger.debug(() -> "Can't find cache service, switching to NoCache instance");
            INSTANCE.set(NoOpCache.INSTANCE);
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

    public boolean isDefault() {
        return getClass().getName().equalsIgnoreCase("io.github.ulisse1996.cache.Caches");
    }
}
