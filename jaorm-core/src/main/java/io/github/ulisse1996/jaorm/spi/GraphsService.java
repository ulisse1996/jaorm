package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultGraphs;
import io.github.ulisse1996.jaorm.spi.provider.GraphProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class GraphsService {

    private static final Singleton<GraphsService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static GraphsService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(
                    INSTANCE.get().getEntityGraphs().keySet().stream().map(GraphPair::getEntity).collect(Collectors.toSet()))) {
                Iterable<GraphProvider> providers = ServiceFinder.loadServices(GraphProvider.class);
                INSTANCE.set(new DefaultGraphs(providers));
            }
        } finally {
            LOCK.unlock();
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
