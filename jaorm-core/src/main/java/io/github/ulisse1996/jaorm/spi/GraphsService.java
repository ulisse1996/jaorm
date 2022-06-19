package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultGraphs;
import io.github.ulisse1996.jaorm.spi.provider.GraphProvider;

import java.util.Map;
import java.util.Optional;

public abstract class GraphsService {

    private static final Singleton<GraphsService> INSTANCE = Singleton.instance();

    public static synchronized GraphsService getInstance() {
        if (!INSTANCE.isPresent()) {
            Iterable<GraphProvider> providers = ServiceFinder.loadServices(GraphProvider.class);
            INSTANCE.set(new DefaultGraphs(providers));
        }

        return INSTANCE.get();
    }

    public Optional<EntityGraph<?>> getEntityGraph(Class<?> entity, String name) { //NOSONAR
        DelegatesService.getInstance().searchDelegate(entity); // Checks for Valid Entity
        return Optional.ofNullable(
                getEntityGraphs().get(new GraphPair(entity, name))
        );
    }

    public abstract Map<GraphPair, EntityGraph<?>> getEntityGraphs(); //NOSONAR
}
