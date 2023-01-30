package io.github.ulisse1996.jaorm.entity;

import java.util.ArrayList;
import java.util.List;

public class DirtinessTracker<T> {

    private final EntityDelegate<T> delegate;
    private final List<Object> removedElements;

    public DirtinessTracker(EntityDelegate<T> delegate) {
        this.delegate = delegate;
        this.removedElements = new ArrayList<>();
    }

    public EntityDelegate<T> getDelegate() {
        return delegate;
    }

    public void registerRemoved(Result<?> result) {
        if (result != null && result.isPresent() && result.get() instanceof EntityDelegate) {
            this.removedElements.add(result.get());
        }
    }

    public void registerRemoved(Object element) {
        if (element instanceof EntityDelegate) {
            this.removedElements.add(element);
        }
    }

    public List<Object> getRemovedElements() {
        return removedElements;
    }
}
