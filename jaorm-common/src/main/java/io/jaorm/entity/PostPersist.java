package io.jaorm.entity;

public interface PostPersist<T, X extends Exception> {

    void postPersist(T entity) throws X;
}
