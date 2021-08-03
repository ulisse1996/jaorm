package io.github.ulisse1996.jaorm.annotation;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

import java.lang.annotation.*;

/**
 * Specifies that column must be converted during select/update of current Entity using
 * {@link ValueConverter} implementation
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@Documented
public @interface Converter {

    /**
     * Implementation class that can convert from/to sql real type. User must provide a valid implementation that
     * return a compatibile instance
     */
    Class<? extends ValueConverter<?, ?>> value(); // NOSONAR We do type check during compilation
}
