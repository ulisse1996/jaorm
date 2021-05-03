package io.github.ulisse1996.jaorm.cache.impl;

import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.cache.JaormAllCache;
import io.github.ulisse1996.jaorm.cache.JaormCache;
import io.github.ulisse1996.jaorm.cache.StandardConfiguration;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class CacheConfigurationTest {

    @Test
    void should_create_standard_cache() {
        CacheConfiguration configuration = new CacheConfiguration();
        Assertions.assertEquals(StandardConfiguration.STANDARD_SIZE, getField("size", configuration));
        Assertions.assertEquals(StandardConfiguration.STANDARD_AFTER_ACCESS, getField("afterAccess", configuration));
        Assertions.assertEquals(StandardConfiguration.STANDARD_AFTER_WRITE, getField("afterWrite", configuration));
        Assertions.assertEquals(false, getField("weakKeys", configuration));
        Assertions.assertEquals(false, getField("weakValues", configuration));
        Assertions.assertEquals(false, getField("softValues", configuration));
    }

    @ParameterizedTest
    @MethodSource("getBuilders")
    void should_create_cache_with_builder(CacheConfiguration.Builder builder) {
        CacheConfiguration configuration = builder.build();

        Assertions.assertEquals(2, getField("size", configuration));
        Assertions.assertEquals(Duration.of(1, ChronoUnit.SECONDS), getField("afterAccess", configuration));
        Assertions.assertEquals(Duration.of(1, ChronoUnit.SECONDS), getField("afterWrite", configuration));
        Assertions.assertEquals(true, getField("weakKeys", configuration));
    }

    @ParameterizedTest
    @MethodSource("getBuilders")
    void should_throw_exception_for_bad_configuration(CacheConfiguration.Builder builder) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.weakValues().softValues()); // NOSONAR
    }

    @Test
    void should_return_missing_value_after_read() {
        CacheConfiguration configuration = new CacheConfiguration();
        Object expected = new Object();
        JaormCache<Object> cache = configuration.getCache(Object.class);
        try (MockedStatic<QueryRunner> mk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> dk = Mockito.mockStatic(DelegatesService.class)) {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            dk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);
            Object o = cache.get(io.github.ulisse1996.jaorm.Arguments.of(1, 2));
            Assertions.assertEquals(expected, o);
        }
    }

    @Test
    void should_return_saved_value_after_read() {
        CacheConfiguration configuration = CacheConfiguration.builder()
                .weakValues()
                .build();
        Object expected = new Object();
        JaormCache<Object> cache = configuration.getCache(Object.class);
        try (MockedStatic<QueryRunner> mk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> dk = Mockito.mockStatic(DelegatesService.class)) {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            dk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);
            Object o = cache.get(io.github.ulisse1996.jaorm.Arguments.of(1, 2));
            Assertions.assertEquals(expected, o);

            o = cache.get(io.github.ulisse1996.jaorm.Arguments.of(1, 2));
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.any(), Mockito.any());
            Assertions.assertEquals(expected, o);
        }
    }

    @Test
    void should_return_missing_values_after_read() {
        CacheConfiguration configuration = new CacheConfiguration();
        List<Object> expected = Collections.singletonList(new Object());
        JaormAllCache<Object> cache = configuration.getAllCache(Object.class);
        try (MockedStatic<QueryRunner> mk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> dk = Mockito.mockStatic(DelegatesService.class)) {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            dk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(runner.readAll(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);
            List<Object> o = cache.getAll();
            Assertions.assertEquals(expected, o);
        }
    }

    @Test
    void should_return_saved_values_after_read() {
        CacheConfiguration configuration = CacheConfiguration.builder()
                .weakValues()
                .build();
        List<Object> expected = Collections.singletonList(new Object());
        JaormAllCache<Object> cache = configuration.getAllCache(Object.class);
        try (MockedStatic<QueryRunner> mk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> dk = Mockito.mockStatic(DelegatesService.class)) {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            dk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(runner.readAll(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);
            List<Object> o = cache.getAll();
            Assertions.assertEquals(expected, o);

            o = cache.getAll();
            Mockito.verify(runner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.any(), Mockito.any());
            Assertions.assertEquals(expected, o);
        }
    }

    private static Stream<Arguments> getBuilders() {
        return Stream.of(
                Arguments.arguments(
                        CacheConfiguration.builder()
                                .maxSize(2)
                                .expireAfterAccess(Duration.of(1, ChronoUnit.SECONDS))
                                .expireAfterWrite(Duration.of(1, ChronoUnit.SECONDS))
                                .weakKeys()
                                .weakValues()
                ),
                Arguments.arguments(
                        CacheConfiguration.builder()
                                .maxSize(2)
                                .expireAfterAccess(Duration.of(1, ChronoUnit.SECONDS))
                                .expireAfterWrite(Duration.of(1, ChronoUnit.SECONDS))
                                .weakKeys()
                                .softValues()
                )
        );
    }

    private Object getField(String fieldName, CacheConfiguration configuration) {
        try {
            Field field = CacheConfiguration.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(configuration);
        } catch (Exception ex) {
            Assertions.fail(ex);
            return null;
        }
    }
}
