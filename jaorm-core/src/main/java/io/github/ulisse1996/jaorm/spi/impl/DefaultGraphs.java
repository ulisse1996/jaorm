package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.GraphsService;
import io.github.ulisse1996.jaorm.spi.provider.GraphProvider;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultGraphs extends GraphsService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultGraphs.class);
    private final Map<GraphPair, EntityGraph<?>> graphs;

    public DefaultGraphs(Iterable<GraphProvider> providers) {
        this.graphs = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(Collectors.toMap(
                                GraphProvider::getPair,
                                GraphProvider::getGraph
                        ))
        );

        logger.debug(() -> String.format("Loaded Graphs %s", graphs.keySet()));
    }

    @Override
    public Map<GraphPair, EntityGraph<?>> getEntityGraphs() {
        return this.graphs;
    }
}
