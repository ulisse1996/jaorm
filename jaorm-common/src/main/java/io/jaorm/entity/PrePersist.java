package io.jaorm.entity;

public interface PrePersist<T, X extends Exception> {

    void prePersist(T entity) throws X;
}
