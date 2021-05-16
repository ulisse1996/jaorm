package io.github.ulisse1996.jaorm.annotation;

/**
 * Type of Event that {@link Cascade} must process
 */
public enum CascadeType {
    ALL,
    PERSIST,
    REMOVE,
    UPDATE
}
