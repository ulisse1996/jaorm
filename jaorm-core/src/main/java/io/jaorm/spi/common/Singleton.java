package io.jaorm.spi.common;

public interface Singleton<T> {

    T get();
    void set(T val);
    boolean isPresent();

    static <T> Singleton<T> instance() {
        return new SingletonImpl<>(null);
    }
}
