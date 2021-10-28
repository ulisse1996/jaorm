package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CombinedProjections extends ProjectionsService {

    private final List<ProjectionsService> projections;

    public CombinedProjections(List<ProjectionsService> services) {
        this.projections = services;
    }

    @Override
    public Map<Class<?>, Supplier<ProjectionDelegate>> getProjections() {
        return Collections.unmodifiableMap(
                this.projections.stream()
                        .flatMap(p -> p.getProjections().entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}
