package io.github.ulisse1996.mapping;

public interface ThrowingFunction<T, R, X extends Exception> {

    R apply(T t) throws X;
}
