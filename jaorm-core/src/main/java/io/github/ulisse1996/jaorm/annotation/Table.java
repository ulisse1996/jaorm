package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Table {

    String name();
    String schema() default UNSET;

    String UNSET = "_UNSET_";
}
