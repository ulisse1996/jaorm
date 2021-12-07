package io.github.ulisse1996.jaorm.graph;

import io.github.ulisse1996.jaorm.spi.GraphsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EntityGraphFetcherTest {

    @Mock private GraphsService graphsService;
    @Mock private EntityGraph<?> graph;

    @Test
    void should_call_fetch_on_graph() {
        Object object = new Object();
        try (MockedStatic<GraphsService> mk = Mockito.mockStatic(GraphsService.class)) {
            mk.when(GraphsService::getInstance)
                    .thenReturn(graphsService);
            Mockito.when(graphsService.getEntityGraph(Mockito.any(), Mockito.any()))
                    .then(invocation -> Optional.of(graph));
            Mockito.when(graph.fetch(Mockito.any()))
                    .then(invocation -> object);
            Object test = EntityGraphFetcher.of(Object.class, "test").fetch(Object.class);
            Assertions.assertEquals(object, test);
        }
    }

    @Test
    void should_call_opt_fetch_on_graph() {
        try (MockedStatic<GraphsService> mk = Mockito.mockStatic(GraphsService.class)) {
            mk.when(GraphsService::getInstance)
                    .thenReturn(graphsService);
            Mockito.when(graphsService.getEntityGraph(Mockito.any(), Mockito.any()))
                    .then(invocation -> Optional.of(graph));
            Mockito.when(graph.fetchOpt(Mockito.any()))
                    .thenReturn(Optional.empty());
            Optional<Object> test = EntityGraphFetcher.of(Object.class, "test").fetchOpt(Object.class);
            Assertions.assertFalse(test.isPresent());
        }
    }
}
