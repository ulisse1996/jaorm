package io.github.ulisse1996.jaorm.annotation;

public interface CustomGenerator<T> {

    T generate(Class<?> columnClass, String columnName);
}
