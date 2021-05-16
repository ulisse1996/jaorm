package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that Core Module must check a different name from the one used by annotated input
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Param {

    /**
     * Referenced name in @{@link Query#sql()}
     */
    String name();
}
