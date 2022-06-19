package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.spi.CacheService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedCaches extends CacheService {

    private final List<CacheService> services;

    public CombinedCaches(List<CacheService> services) {
        this.services = Collections.unmodifiableList(services);
    }

    @Override
    public <T> boolean isCacheable(Class<T> klass) {
        return services.stream().anyMatch(c -> c.isCacheable(klass));
    }

    @Override
    public Map<Class<?>, EntityCache<?>> getCaches() {
        Map<Class<?>, EntityCache<?>> map = new HashMap<>();
        for (CacheService cacheService : services) {
            map.putAll(cacheService.getCaches());
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
