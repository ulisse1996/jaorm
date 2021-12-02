package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

/**
 * Specifies that annotated method must be implemented by Core Module
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Query {

    /**
     * Sql that must be executed by Core Module
     */
    String sql();

    /**
     * Specifies that this query is a mutation that doesn't need arguments
     */
    boolean noArgs() default false;
}
