package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.GraphsService;

import java.util.List;
import java.util.Map;

public class CombinedGraphs extends GraphsService {

    private final List<GraphsService> graphsServices;

    public CombinedGraphs(List<GraphsService> graphsServices) {
        this.graphsServices = graphsServices;
    }

    @Override
    public Map<GraphPair, EntityGraph<?>> getEntityGraphs() {
        return this.graphsServices.stream()
                .flatMap(m -> m.getEntityGraphs().entrySet().stream())
                .collect(Combiners.getEntryMapCollector());
    }
}
