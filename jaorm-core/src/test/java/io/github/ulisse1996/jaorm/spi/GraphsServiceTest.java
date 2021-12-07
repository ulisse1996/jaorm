package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.DelegatesMock;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.combined.CombinedGraphs;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
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
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;

@ExtendWith(MockitoExtension.class)
class GraphsServiceTest {

    @Mock private GraphsService graphsService;
    @Mock private EntityGraph<?> graph;
    @Mock private DelegatesService delegatesService;

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
    void should_not_find_instances() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(GraphsService.class))
                    .thenReturn(Collections.emptyIterator());
            Assertions.assertTrue(
                    GraphsService.getInstance() instanceof GraphsService.NoOp
            );
            Assertions.assertTrue(GraphsService.getInstance().getEntityGraphs().isEmpty());
        }
    }

    @Test
    void should_return_simple_service() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphsService.class))
                    .thenReturn(Collections.singletonList(graphsService));
            Assertions.assertEquals(
                    graphsService,
                    GraphsService.getInstance()
            );
        }
    }

    @Test
    void should_return_combined_service() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphsService.class))
                    .thenReturn(Collections.nCopies(3, graphsService));
            Assertions.assertTrue(
                    GraphsService.getInstance() instanceof CombinedGraphs
            );
        }
    }

    @Test
    void should_set_no_op_for_exception() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphsService.class))
                    .thenThrow(ServiceConfigurationError.class);
            Assertions.assertTrue(
                    GraphsService.getInstance() instanceof GraphsService.NoOp
            );
        }
    }

    @Test
    void should_return_empty_graph() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphsService.class))
                    .thenReturn(Collections.singletonList(new MyGraphService()));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .thenReturn(() -> null);
            Assertions.assertFalse(
                    GraphsService.getInstance() instanceof GraphsService.NoOp
            );
            Assertions.assertFalse(
                    GraphsService.getInstance().getEntityGraph(Object.class, "test").isPresent()
            );
        }
    }

    @Test
    void should_return_graph() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(() -> ServiceFinder.loadServices(GraphsService.class))
                    .thenReturn(Collections.singletonList(new MyGraphService()));
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .thenReturn(() -> null);
            Assertions.assertFalse(
                    GraphsService.getInstance() instanceof GraphsService.NoOp
            );
            Optional<EntityGraph<?>> opt = GraphsService.getInstance().getEntityGraph(DelegatesMock.MyEntity.class, "test");
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(graph, opt.get());
        }
    }

    private class MyGraphService extends GraphsService {

        @Override
        public Map<GraphPair, EntityGraph<?>> getEntityGraphs() {
            return Collections.singletonMap(new GraphPair(DelegatesMock.MyEntity.class, "test"), graph);
        }
    }
}
