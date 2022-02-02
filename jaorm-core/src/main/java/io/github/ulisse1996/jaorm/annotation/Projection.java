package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Projection {

    String schema() default UNSET;

    String UNSET = "_UNSET_";
}
