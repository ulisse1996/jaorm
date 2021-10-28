package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.spi.ConverterService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedConverters extends ConverterService {

    private final Map<Class<?>, ConverterPair<?, ?>> converters;

    public CombinedConverters(List<ConverterService> services) {
        Map<Class<?>, ConverterPair<?, ?>> pairs = new HashMap<>();
        for (ConverterService service : services) {
            Map<Class<?>, ConverterPair<?, ?>> curr = service.getConverters();
            pairs.putAll(curr);
        }
        this.converters = Collections.unmodifiableMap(pairs);
    }

    @Override
    public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
        return converters;
    }
}
