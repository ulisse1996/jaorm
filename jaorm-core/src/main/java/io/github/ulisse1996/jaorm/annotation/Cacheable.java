package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

/**
 * Specifies that current Entity can be cached by Cache Module
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Cacheable {}
