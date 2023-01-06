package io.github.ulisse1996.jaorm.spi.common;

public interface Singleton<T> {

    T get();
    void set(T val);
    boolean isPresent();

    static <T> Singleton<T> instance() {
        return instance(null);
    }

    static <T> Singleton<T> instance(T value) {
        return new SingletonImpl<>(value);
    }
}
