package io.github.ulisse1996.jaorm.spi.provider;

import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;

public interface GraphProvider {

    GraphPair getPair();
    EntityGraph<?> getGraph(); //NOSONAR
}
