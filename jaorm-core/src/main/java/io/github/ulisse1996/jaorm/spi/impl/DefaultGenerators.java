package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;
import io.github.ulisse1996.jaorm.spi.provider.GeneratorProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultGenerators extends GeneratorsService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultGenerators.class);
    private final Map<Class<?>, List<GenerationInfo>> generators;

    public DefaultGenerators(Iterable<GeneratorProvider> providers) {
        this.generators = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(Collectors.toMap(
                                GeneratorProvider::getEntityClass,
                                e -> new ArrayList<>(e.getInfo())
                        ))
        );

        logger.debug(() -> String.format("Loaded Generators for %s", generators.keySet()));
    }

    @Override
    public Map<Class<?>, List<GenerationInfo>> getGenerated() {
        return this.generators;
    }
}
