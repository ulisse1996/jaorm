package io.jaorm.custom;

public interface CustomFeature<T> {

    T getFeature();
    boolean isEnabled();
}
