package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

/**
 * Specifies that annotated class should be treated as a DAO (Data Access Object) by Core Module
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Dao {}
