package io.github.ulisse1996.custom;

public interface CustomFeature<T> {

    T getFeature();
    boolean isEnabled();
}
