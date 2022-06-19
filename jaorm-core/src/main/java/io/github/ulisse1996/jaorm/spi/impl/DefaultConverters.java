package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.ConverterService;
import io.github.ulisse1996.jaorm.spi.provider.ConverterProvider;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultConverters extends ConverterService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultConverters.class);
    private final Map<Class<?>, ConverterPair<?, ?>> converters;

    @SuppressWarnings("unchecked")
    public DefaultConverters(Iterable<ConverterProvider> providers) {
        this.converters = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(Collectors.toMap(
                                ConverterProvider::to,
                                e -> new ConverterPair<>((Class<Object>) e.from(), (ValueConverter<Object, Object>) e.converter())))
        );
        logger.debug(() -> String.format("Loaded Converters for %s", this.converters.entrySet()));
    }

    @Override
    public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
        return this.converters;
    }
}
