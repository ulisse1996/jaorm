package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.combined.CombinedCaches;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;

class CacheServiceTest {

    private final JaormCache<?> cache = Mockito.mock(JaormCache.class);
    private final JaormAllCache<?> allCache = Mockito.mock(JaormAllCache.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetCache() {
        try {
            Field instance = CacheService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<CacheService> o = (Singleton<CacheService>) instance.get(null);
            o.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_mocked_cache() {
        Assertions.assertEquals(cache, getInstance().getCache(Object.class));
    }

    @Test
    void should_return_mocked_cache_all() {
        Assertions.assertEquals(allCache, getInstance().getCacheAll(Object.class));
    }

    @Test
    void should_return_no_instance_cache() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenThrow(IllegalArgumentException.class);
            Assertions.assertSame(NoOpCache.INSTANCE, CacheService.getInstance());
        }
    }

    @Test
    void should_return_custom_implementation_with_multiple_services() {
        CacheService mock = Mockito.spy(CacheService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenReturn(Collections.nCopies(2, mock));
            Mockito.when(mock.isDefault())
                    .thenReturn(true);

            CacheService instance = CacheService.getInstance();
            Assertions.assertEquals(instance.getClass(), CombinedCaches.class);
            Assertions.assertTrue(instance.isDefault());
        }
    }

    @Test
    void should_return_custom_implementation() {
        CacheService mock = Mockito.spy(CacheService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenReturn(Collections.singletonList(mock));

            CacheService instance = CacheService.getInstance();
            Assertions.assertEquals(mock, instance);
            Assertions.assertFalse(instance.isDefault());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_change_cache() {
        JaormCache<Object> cache = new JaormCache<Object>() {
            @Override
            public Object get(Arguments arguments) {
                return null;
            }

            @Override
            public Optional<Object> getOpt(Arguments arguments) {
                return Optional.empty();
            }
        };
        CacheService instance = getInstance();
        instance.getCaches().put(Object.class, new EntityCache<>(cache, (JaormAllCache<Object>) allCache));
        Assertions.assertEquals(cache, instance.getCache(Object.class));
    }

    @Test
    void should_set_new_cache_configuration() {
        JaormCache<Object> cache = new JaormCache<Object>() {
            @Override
            public Object get(Arguments arguments) {
                return null;
            }

            @Override
            public Optional<Object> getOpt(Arguments arguments) {
                return Optional.empty();
            }
        };
        JaormAllCache<Object> allCache = () -> null;
        AbstractCacheConfiguration configuration = new AbstractCacheConfiguration() {

            @Override
            @SuppressWarnings("unchecked")
            protected <T> JaormCache<T> getCache(Class<T> klass) {
                return (JaormCache<T>) cache;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected <T> JaormAllCache<T> getAllCache(Class<T> klass) {
                return (JaormAllCache<T>) allCache;
            }
        };
        CacheService instance = getInstance();
        instance.setConfiguration(Object.class, configuration);
        Assertions.assertEquals(cache, instance.getCache(Object.class));
        Assertions.assertEquals(allCache, instance.getCacheAll(Object.class));
    }

    @Test
    void should_return_combined_cache() {
        CacheService mock = Mockito.mock(CacheService.class);
        Mockito.when(mock.isDefault())
                .thenReturn(true);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenReturn(Collections.nCopies(3, mock));
            Assertions.assertTrue(CacheService.getInstance() instanceof CombinedCaches);
        }
    }

    @Test
    void should_return_no_op_for_multiple_not_defaults() {
        CacheService mock = Mockito.mock(CacheService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenReturn(Collections.nCopies(3, mock));
            Assertions.assertTrue(CacheService.getInstance() instanceof NoOpCache);
        }
    }

    @Test
    void should_return_no_op_for_bad_configuration() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(CacheService.class))
                    .thenThrow(ServiceConfigurationError.class);
            Assertions.assertTrue(CacheService.getInstance() instanceof NoOpCache);
        }
    }

    private CacheService getInstance() {
        return new CacheService() {

            private final Map<Class<?>, EntityCache<?>> map = new ConcurrentHashMap<>();

            {
                AbstractCacheConfiguration configuration = Mockito.mock(AbstractCacheConfiguration.class);
                Mockito.when(configuration.getCache(Mockito.any())).then(invocation -> cache);
                Mockito.when(configuration.getAllCache(Mockito.any())).then(invocation -> allCache);
                map.put(Object.class, EntityCache.fromConfiguration(Object.class, configuration));
            }

            @Override
            public <T> boolean isCacheable(Class<T> klass) {
                return true;
            }

            @Override
            public Map<Class<?>, EntityCache<?>> getCaches() {
                return map;
            }
        };
    }


}
