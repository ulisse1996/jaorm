package io.github.ulisse1996.jaorm.annotation;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;

import java.lang.annotation.*;

/**
 * Specifies that annotated field is a Linked Entity that could be lazily fetched.
 * Annotated field type should be:
 * <ul>
 *     <li>an {@link Result OptionalEntity} instance for an Optional One To One Relationship</li>
 *     <li>a {@link java.util.List List} for a One To Many or Many to Many Relationship</li>
 *     <li>am Entity Instance for a One To One Relationship</li>
 * </ul>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@Documented
public @interface Relationship {

    RelationshipColumn[] columns();
    int priority() default 0;

    @interface RelationshipColumn {

        int NONE = 0;

        String defaultValue() default "";
        ParameterConverter converter() default ParameterConverter.NONE;
        String sourceColumn() default "";
        String targetColumn();
    }
}
