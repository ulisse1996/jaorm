package io.github.ulisse1996.annotation;

import io.github.ulisse1996.entity.converter.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Converter {

    Class<? extends ValueConverter<?, ?>> value(); // NOSONAR We do types check during compilation
}
