package io.github.ulisse1996.jaorm.dsl.util;

public class Pair<T, R> {

    private final T key;
    private final R value;

    public Pair(T key, R value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public R getValue() {
        return value;
    }
}
