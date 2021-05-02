package io.github.ulisse1996.entity.event;

public interface PostRemove<X extends Exception> {

    void postRemove() throws X;
}
