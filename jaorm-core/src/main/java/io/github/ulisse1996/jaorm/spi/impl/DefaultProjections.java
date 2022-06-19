package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultProjections extends ProjectionsService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultProjections.class);
    private final Map<Class<?>, Supplier<ProjectionDelegate>> delegates;

    public DefaultProjections(Iterable<ProjectionDelegate> providers) {
        this.delegates = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(Collectors.toMap(
                                ProjectionDelegate::getProjectionClass,
                                e -> e::getInstance
                        ))
        );

        logger.debug(() -> String.format("Loaded projections %s", delegates.keySet()));
    }

    @Override
    public Map<Class<?>, Supplier<ProjectionDelegate>> getProjections() {
        return this.delegates;
    }
}
