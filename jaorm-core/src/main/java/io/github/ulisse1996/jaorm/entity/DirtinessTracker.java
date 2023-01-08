package io.github.ulisse1996.jaorm.entity;

public class DirtinessTracker {

    private final EntityDelegate<?> obj;

    public DirtinessTracker(EntityDelegate<?> obj) {
        this.obj = obj;
    }
}
