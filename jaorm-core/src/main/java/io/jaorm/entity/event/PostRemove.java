package io.jaorm.entity.event;

public interface PostRemove<X extends Exception> {

    void postRemove() throws X;
}
