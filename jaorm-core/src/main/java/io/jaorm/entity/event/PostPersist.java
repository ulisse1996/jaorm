package io.jaorm.entity.event;

public interface PostPersist<X extends Exception> {

    void postPersist() throws X;
}
