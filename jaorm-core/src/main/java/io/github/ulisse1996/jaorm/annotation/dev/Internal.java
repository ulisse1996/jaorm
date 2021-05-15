package io.github.ulisse1996.jaorm.annotation.dev;


import java.lang.annotation.*;

/**
 * Indicates to the user that annotated element should not be used directly
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Internal {
}
