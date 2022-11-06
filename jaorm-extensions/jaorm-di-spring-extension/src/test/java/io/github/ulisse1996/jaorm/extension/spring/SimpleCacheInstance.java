package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.spi.CacheService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
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
