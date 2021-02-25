package io.jaorm.entity.event;

public interface PreUpdate<X extends Exception> {

    void preUpdate() throws X;
}
