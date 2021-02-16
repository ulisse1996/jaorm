package io.jaorm.entity.event;

public interface PrePersist<X extends Exception> {

    void prePersist() throws X;
}
