package io.github.ulisse1996.jaorm.entity.event;

public interface PrePersist<X extends Exception> {

    void prePersist() throws X;
}
