package io.github.ulisse1996.jaorm.custom;

public interface CustomFeature<T> {

    T getFeature();
    boolean isEnabled();
}
