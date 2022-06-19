package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.cache.EntityCache;
import io.github.ulisse1996.jaorm.spi.CacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CombinedCachesTest {

    @Mock private EntityCache<?> entityCache;
    @Mock private CacheService cacheService;
    private CombinedCaches caches;

    @BeforeEach
    void init() {
        this.caches = new CombinedCaches(Collections.singletonList(cacheService));
    }

    @Test
    void should_return_true_for_default_impl() {
        Assertions.assertTrue(caches.isDefault());
    }

    @Test
    void should_return_all_caches() {
        Map<Class<?>, EntityCache<?>> c = new HashMap<>();
        c.put(Object.class, entityCache);

        Mockito.when(cacheService.getCaches()).thenReturn(c);
        Assertions.assertEquals(
                c,
                caches.getCaches()
        );
    }

    @Test
    void should_return_false_for_not_valid_cache() {
        Mockito.when(cacheService.isCacheable(Mockito.any())).thenReturn(false);
        Assertions.assertFalse(caches.isCacheable(Object.class));
    }
}
