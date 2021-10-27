package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.spi.ConverterService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedConverters extends ConverterService {

    private final Map<Class<?>, ConverterPair<?, ?>> converters;

    public CombinedConverters(List<ConverterService> services) {
        this.converters = services.stream()
                .flatMap(s -> s.getConverters().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
        return Collections.unmodifiableMap(converters);
    }
}
