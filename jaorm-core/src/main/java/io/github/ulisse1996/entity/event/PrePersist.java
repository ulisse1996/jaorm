package io.github.ulisse1996.entity.event;

public interface PrePersist<X extends Exception> {

    void prePersist() throws X;
}
