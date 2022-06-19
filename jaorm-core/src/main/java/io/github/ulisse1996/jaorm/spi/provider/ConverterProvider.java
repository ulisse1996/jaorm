package io.github.ulisse1996.jaorm.spi.provider;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

public interface ConverterProvider {

    Class<?> from();
    Class<?> to();
    ValueConverter<?, ?> converter(); //NOSONAR
}
