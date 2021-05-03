package io.github.ulisse1996.jaorm.cache;

public class EntityCache<S> {

    private final JaormCache<S> cache;
    private final JaormAllCache<S> allCache;

    public EntityCache(JaormCache<S> cache, JaormAllCache<S> allCache) {
        this.cache = cache;
        this.allCache = allCache;
    }

    public JaormAllCache<S> getAllCache() {
        return allCache;
    }

    public JaormCache<S> getCache() {
        return cache;
    }

    public static <S> EntityCache<S> fromConfiguration(Class<S> klass, AbstractCacheConfiguration cacheConfiguration) {
        return new EntityCache<>(cacheConfiguration.getCache(klass), cacheConfiguration.getAllCache(klass));
    }
}
