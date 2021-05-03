package io.github.ulisse1996.jaorm.entity.event;

public interface PreUpdate<X extends Exception> {

    void preUpdate() throws X;
}
