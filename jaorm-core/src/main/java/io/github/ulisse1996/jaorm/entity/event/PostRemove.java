package io.github.ulisse1996.jaorm.entity.event;

public interface PostRemove<X extends Exception> {

    void postRemove() throws X;
}
