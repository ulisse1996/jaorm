package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.GraphsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
class CombinedGraphsTest {

    @Mock private GraphsService g1;
    @Mock private EntityGraph<?> entityGraph;
    @Mock private GraphsService g2;

    @Test
    void should_return_all_graphs() {
        Mockito.when(g1.getEntityGraphs())
                .thenReturn(Collections.singletonMap(new GraphPair(Object.class, "t1"), entityGraph));
        Mockito.when(g2.getEntityGraphs())
                .thenReturn(Collections.singletonMap(new GraphPair(Object.class, "t2"), entityGraph));
        CombinedGraphs combinedGraphs = new CombinedGraphs(Arrays.asList(g1, g2));
        Assertions.assertEquals(
                new HashSet<>(Arrays.asList(new GraphPair(Object.class, "t1"), new GraphPair(Object.class, "t2"))),
                combinedGraphs.getEntityGraphs().keySet()
        );
    }

}
