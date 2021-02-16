package io.jaorm;

import io.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

class QueryRunnerTest {

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
}