package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultGraphs;
import io.github.ulisse1996.jaorm.spi.provider.GraphProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GraphsServiceTest {

    @Mock private EntityGraph<?> graph;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        try {
            Field field = GraphsService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<GraphsService> instance = (Singleton<GraphsService>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_default_impl() {
        GraphProvider provider = Mockito.mock(GraphProvider.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphProvider.class))
                    .thenReturn(Collections.singletonList(provider));
            Mockito.when(provider.getGraph()).then(invocation -> graph);
            Mockito.when(provider.getPair()).thenReturn(new GraphPair(Object.class, "NAME"));

            GraphsService service = GraphsService.getInstance();

            Assertions.assertTrue(service instanceof DefaultGraphs);
        }
    }

    @Test
    void should_return_same_instance() {
        GraphProvider provider = Mockito.mock(GraphProvider.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphProvider.class))
                    .thenReturn(Collections.singletonList(provider));
            Mockito.when(provider.getGraph()).then(invocation -> graph);
            Mockito.when(provider.getPair()).thenReturn(new GraphPair(Object.class, "NAME"));

            GraphsService service = GraphsService.getInstance();

            Assertions.assertSame(service, GraphsService.getInstance());
        }
    }

    @Test
    void should_return_entity_graph() {
        Map<GraphPair, EntityGraph<?>> graphMap = new HashMap<>();
        GraphPair pair = new GraphPair(Object.class, "NAME");
        graphMap.put(pair, graph);

        GraphsService service = new GraphsService() {
            @Override
            public Map<GraphPair, EntityGraph<?>> getEntityGraphs() {
                return graphMap;
            }
        };
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);

            Optional<EntityGraph<?>> opt = service.getEntityGraph(Object.class, "NAME");

            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(graph, opt.get());
        }
    }
}
