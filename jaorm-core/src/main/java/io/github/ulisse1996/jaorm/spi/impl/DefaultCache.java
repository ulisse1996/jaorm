package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.provider.CacheActivator;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultCache extends CacheService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultCache.class);
    private final Map<Class<?>, EntityCache<?>> cacheMap;
    private final Set<Class<?>> classes;

    public DefaultCache(Iterable<CacheActivator> loadServices) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.classes = Collections.unmodifiableSet(
                StreamSupport.stream(loadServices.spliterator(), false)
                        .map(CacheActivator::getEntityClass)
                        .collect(Collectors.toSet())
        );

        logger.debug(() -> String.format("Loaded cache activation for %s", classes));
    }

    @Override
    public <T> boolean isCacheable(Class<T> klass) {
        return classes.stream()
                .anyMatch(el -> ClassChecker.isAssignable(el, klass));
    }

    @Override
    public Map<Class<?>, EntityCache<?>> getCaches() {
        return this.cacheMap;
    }
}
