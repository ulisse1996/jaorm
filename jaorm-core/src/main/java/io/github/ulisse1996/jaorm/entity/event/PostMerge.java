package io.github.ulisse1996.jaorm.entity.event;

public interface PostMerge<X extends Exception> {

    void postMerge() throws X;
}
