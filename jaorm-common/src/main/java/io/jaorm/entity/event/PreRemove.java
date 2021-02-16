package io.jaorm.entity.event;

public interface PreRemove<X extends Exception> {

    void preRemove() throws X;
}
