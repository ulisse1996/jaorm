package io.github.ulisse1996.jaorm.annotation;

public interface CustomGenerator<T> {

    T generate(Class<?> entityClass, Class<?> columnClass, String columnName);
}
