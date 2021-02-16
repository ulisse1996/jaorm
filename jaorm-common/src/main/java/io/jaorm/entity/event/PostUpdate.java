package io.jaorm.entity.event;

public interface PostUpdate<X extends Exception> {

    void postUpdate() throws X;
}
