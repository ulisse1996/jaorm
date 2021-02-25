package io.jaorm;

import io.jaorm.spi.QueryRunner;
import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

class QueryRunnerTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        // Reset all singleton
        try {
            Field simpleRunner = QueryRunner.class.getDeclaredField("SIMPLE_RUNNER");
            Field entityRunner = QueryRunner.class.getDeclaredField("ENTITY_RUNNER");
            simpleRunner.setAccessible(true);
            entityRunner.setAccessible(true);
            ((Singleton<QueryRunner>)simpleRunner.get(null)).set(null);
            ((Singleton<QueryRunner>)entityRunner.get(null)).set(null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    void should_not_find_query_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertThrows(IllegalArgumentException.class, () -> QueryRunner.getInstance(Object.class));
        }
    }

    @Test
    void should_not_find_simple_runner() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.singletonList(new MockedRunner()));
            Assertions.assertThrows(IllegalArgumentException.class, QueryRunner::getSimple);
        }
    }

    @Test
    void should_find_simple_runner() {
        SimpleMockedRunner expected = new SimpleMockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Collections.singletonList(expected));
            QueryRunner runner = QueryRunner.getSimple();
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_find_custom_runner() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            QueryRunner runner = QueryRunner.getInstance(String.class);
            Assertions.assertEquals(expected, runner);
        }
    }

    @Test
    void should_call_service_load_for_entity_runner_only_first_time() {
        MockedRunner expected = new MockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            QueryRunner runner = QueryRunner.getInstance(String.class);
            Assertions.assertEquals(expected, runner);
            QueryRunner runner1 = QueryRunner.getInstance(String.class);
            Assertions.assertEquals(runner, runner1);
            mk.verify(() -> ServiceFinder.loadServices(QueryRunner.class));
        }
    }

    @Test
    void should_call_service_load_for_simple_runner_only_first_time() {
        SimpleMockedRunner expected = new SimpleMockedRunner();
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryRunner.class))
                    .thenReturn(Arrays.asList(new SimpleMockedRunner(), expected));
            QueryRunner runner = QueryRunner.getSimple();
            Assertions.assertEquals(expected, runner);
            QueryRunner runner1 = QueryRunner.getSimple();
            Assertions.assertEquals(runner, runner1);
            mk.verify(() -> ServiceFinder.loadServices(QueryRunner.class));
        }
    }
}