package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.MockedProvider;
import io.github.ulisse1996.jaorm.spi.CacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockedProvider.class)
class CacheableTest {

    @Test
    void should_return_cached_value() {
        try (MockedStatic<CacheService> mk = Mockito.mockStatic(CacheService.class)) {
            Object expected = new Object();
            CacheService cacheService = Mockito.mock(CacheService.class);
            JaormCache<?> cache = Mockito.mock(JaormCache.class);
            mk.when(CacheService::getInstance)
                    .thenReturn(cacheService);
            Mockito.when(cacheService.isCacheActive())
                    .thenReturn(true);
            Mockito.when(cacheService.isCacheable(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(cacheService.getCache(Mockito.any()))
                    .then(invocation -> cache);
            Mockito.when(cacheService.get(Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            Mockito.when(cache.get(Mockito.any()))
                    .then(invocation -> expected);
            Object result = Cacheable.getCached(Object.class, Arguments.empty(), () -> null);
            Assertions.assertEquals(expected, result);
        }
    }

    @Test
    void should_return_cached_values() {
        try (MockedStatic<CacheService> mk = Mockito.mockStatic(CacheService.class)) {
            List<Object> expected = Collections.singletonList(new Object());
            CacheService cacheService = Mockito.mock(CacheService.class);
            JaormAllCache<?> cache = Mockito.mock(JaormAllCache.class);
            mk.when(CacheService::getInstance)
                    .thenReturn(cacheService);
            Mockito.when(cacheService.isCacheActive())
                    .thenReturn(true);
            Mockito.when(cacheService.isCacheable(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(cacheService.getCacheAll(Mockito.any()))
                    .then(invocation -> cache);
            Mockito.when(cacheService.getAll(Mockito.any()))
                    .thenCallRealMethod();
            Mockito.when(cache.getAll())
                    .then(invocation -> expected);
            List<Object> result = Cacheable.getCachedAll(Object.class, () -> null);
            Assertions.assertEquals(expected, result);
        }
    }

    @Test
    void should_return_cached_opt_value() {
        try (MockedStatic<CacheService> mk = Mockito.mockStatic(CacheService.class)) {
            Object expected = new Object();
            CacheService cacheService = Mockito.mock(CacheService.class);
            JaormCache<?> cache = Mockito.mock(JaormCache.class);
            mk.when(CacheService::getInstance)
                    .thenReturn(cacheService);
            Mockito.when(cacheService.isCacheActive())
                    .thenReturn(true);
            Mockito.when(cacheService.isCacheable(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(cacheService.getCache(Mockito.any()))
                    .then(invocation -> cache);
            Mockito.when(cacheService.getOpt(Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            Mockito.when(cache.getOpt(Mockito.any()))
                    .then(invocation -> Optional.of(expected));
            Optional<Object> result = Cacheable.getCachedOpt(Object.class, Arguments.empty(), Optional::empty);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(expected, result.get());
        }
    }

    @Test
    void should_return_executor_value() {
        Object expected = new Object();
        Object result = Cacheable.getCached(Object.class, Arguments.empty(), () -> expected);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void should_return_executor_values() {
        List<Object> expected = Collections.singletonList(new Object());
        List<Object> result = Cacheable.getCachedAll(Object.class, () -> expected);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void should_return_executor_opt_value() {
        Object expected = new Object();
        Optional<Object> result = Cacheable.getCachedOpt(Object.class, Arguments.empty(), () -> Optional.of(expected));
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expected, result.get());
    }
}
