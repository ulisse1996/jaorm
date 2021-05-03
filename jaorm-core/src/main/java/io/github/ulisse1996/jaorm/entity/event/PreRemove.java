package io.github.ulisse1996.jaorm.entity.event;

public interface PreRemove<X extends Exception> {

    void preRemove() throws X;
}
