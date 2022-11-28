package io.github.ulisse1996.jaorm.entity.tracker;

import io.github.ulisse1996.jaorm.entity.ChangeTracker;

import java.util.AbstractList;
import java.util.List;

public class RelationshipList<T, R> extends AbstractList<T> {

    private final R parent;
    private final List<T> delegate;

    public RelationshipList(R parent, List<T> delegate) {
        this.delegate = delegate;
        this.parent = parent;
    }

    @Override
    public T get(int index) {
        return this.delegate.get(index);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public T set(int index, T element) {
        return this.delegate.set(index, element);
    }

    @Override
    public boolean add(T t) {
        return this.delegate.add(t);
    }

    @Override
    public T remove(int index) {
        T element = this.delegate.remove(index);
        ChangeTracker.getInstance().addChange(this.parent, element);
        return element;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        List<T> subList = super.subList(fromIndex, toIndex);
        return new RelationshipList<>(this.parent, subList);
    }
}
