package io.github.ulisse1996.jaorm.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoCacheTest {

    @Test
    void should_return_null_cache() {
        Assertions.assertNull(NoOpCache.INSTANCE.getCache(Object.class));
    }

    @Test
    void should_return_null_all_cache() {
        Assertions.assertNull(NoOpCache.INSTANCE.getCacheAll(Object.class));
    }

    @Test
    void should_return_cache_not_active() {
        Assertions.assertFalse(NoOpCache.INSTANCE.isCacheActive());
    }

    @Test
    void should_return_false_cacheable() {
        Assertions.assertFalse(NoOpCache.INSTANCE.isCacheable(Object.class));
    }

    @Test
    void should_return_empty_map() {
        Assertions.assertTrue(NoOpCache.INSTANCE.getCaches().isEmpty());
    }

    @Test
    void should_do_nothing_for_cache_configuration_set() {
        AbstractCacheConfiguration configuration = new AbstractCacheConfiguration() {
            @Override
            protected <T> JaormCache<T> getCache(Class<T> klass) {
                return null;
            }

            @Override
            protected <T> JaormAllCache<T> getAllCache(Class<T> klass) {
                return null;
            }
        };
        Assertions.assertDoesNotThrow(() -> NoOpCache.INSTANCE.setConfiguration(Object.class, configuration));
    }
}
