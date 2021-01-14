package io.jaorm.processor.annotation;

import io.jaorm.entity.converter.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Converter {

    Class<? extends ValueConverter<?, ?>> value();
}
