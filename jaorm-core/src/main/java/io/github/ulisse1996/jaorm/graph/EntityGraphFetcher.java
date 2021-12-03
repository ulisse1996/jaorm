package io.github.ulisse1996.jaorm.graph;

import io.github.ulisse1996.jaorm.spi.GraphsService;

import java.util.Optional;

public class EntityGraphFetcher<T> {

    private final EntityGraph<T> entityGraph;

    @SuppressWarnings("unchecked")
    private EntityGraphFetcher(Class<T> klass, String name) {
        this.entityGraph = (EntityGraph<T>) GraphsService.getInstance().getEntityGraph(klass, name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Can't find graph for %s with name %s", klass.getName(), name)));
    }

    public static <T> EntityGraphFetcher<T> of(Class<T> klass, String name) {
        return new EntityGraphFetcher<>(klass, name);
    }

    public T fetch(T entity) {
        return entityGraph.fetch(entity);
    }

    public Optional<T> fetchOpt(T entity) {
        return entityGraph.fetchOpt(entity);
    }
}
