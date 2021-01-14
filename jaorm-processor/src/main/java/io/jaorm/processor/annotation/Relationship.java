package io.jaorm.processor.annotation;

import io.jaorm.processor.util.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Relationship {

    RelationshipColumns[] columns();

    @interface RelationshipColumns {

        int NONE = 0;

        String defaultValue() default "";
        Converter converter() default Converter.NONE;
        String sourceColumn() default "";
        String targetColumn();
    }
}
