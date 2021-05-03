package io.github.ulisse1996.jaorm.entity.event;

public interface PostUpdate<X extends Exception> {

    void postUpdate() throws X;
}
