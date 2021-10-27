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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CombinedCachesTest {

    @Mock private CacheService mock1;
    @Mock private CacheService mock2;
    private CombinedCaches combinedCaches;

    @BeforeEach
    void init() {
        this.combinedCaches = new CombinedCaches(Arrays.asList(mock1, mock2));
    }

    @Test
    void should_return_cacheable_only_on_one_service() {
        Mockito.when(mock2.isCacheable(Mockito.any()))
                .thenReturn(true);
        Assertions.assertTrue(combinedCaches.isCacheable(Mockito.any()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_combined_cache() {
        Mockito.when(mock1.getCaches())
                .thenReturn(Collections.singletonMap(String.class, Mockito.mock(EntityCache.class)));
        Mockito.when(mock2.getCaches())
                .thenReturn(Collections.singletonMap(BigDecimal.class, Mockito.mock(EntityCache.class)));
        Map<Class<?>, EntityCache<?>> result = combinedCaches.getCaches();
        Assertions.assertEquals(2, result.keySet().size());
    }
}
