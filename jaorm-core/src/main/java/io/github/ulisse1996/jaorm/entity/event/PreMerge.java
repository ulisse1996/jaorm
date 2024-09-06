package io.github.ulisse1996.jaorm.entity.event;

public interface PreMerge<X extends Exception> {

    void preMerge() throws X;
}
