package io.github.ulisse1996.jaorm.annotation;

import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface TableGenerated {

    String tableName();
    String keyColumn();
    String valueColumn();
    String matchKey();
    ParameterConverter matchConverter() default ParameterConverter.NONE;
}
