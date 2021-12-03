package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.spi.combined.CombinedGraphs;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class GraphsService {

    private static final Singleton<GraphsService> INSTANCE = Singleton.instance();

    public static synchronized GraphsService getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                List<GraphsService> services = StreamSupport.stream(ServiceFinder.loadServices(GraphsService.class).spliterator(), false)
                        .collect(Collectors.toList());
                if (services.isEmpty()) {
                    INSTANCE.set(NoOp.INSTANCE);
                } else {
                    if (services.size() > 1) {
                        INSTANCE.set(new CombinedGraphs(services));
                    } else {
                        INSTANCE.set(services.get(0));
                    }
                }
            } catch (Exception ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
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

    private static class NoOp extends GraphsService {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public Map<GraphPair, EntityGraph<?>> getEntityGraphs() {
            return Collections.emptyMap();
        }
    }
}
