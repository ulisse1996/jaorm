package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.converter.BooleanIntConverter;
import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.spi.ConverterService;

import java.util.Collections;
import java.util.Map;

public class ConverterMock extends ConverterService {

    @Override
    public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
        return Collections.singletonMap(Boolean.class, new ConverterPair<>(Integer.class, BooleanIntConverter.INSTANCE));
    }
}
