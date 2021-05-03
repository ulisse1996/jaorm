package io.github.ulisse1996.jaorm.entity.event;

public interface PostPersist<X extends Exception> {

    void postPersist() throws X;
}
