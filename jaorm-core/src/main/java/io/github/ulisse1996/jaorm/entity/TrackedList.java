package io.github.ulisse1996.jaorm.entity;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrackedList<T> extends AbstractList<T> {

    private final List<T> delegate;
    private final List<T> removedElements;

    public TrackedList(List<T> delegate) {
        this(delegate, new ArrayList<>());
    }

    public TrackedList(List<T> delegate, List<T> removedElements) {
        this.delegate = new ArrayList<>(delegate);
        this.removedElements = new ArrayList<>(removedElements);
    }

    public static <T> TrackedList<T> merge(List<T> from, List<T> newList) {
        if (from == null) {
            return new TrackedList<>(newList, new ArrayList<>());
        }
        if (from instanceof TrackedList) {
            // Override of current state is equals to clear, so we get all elements (previously deleted or not) and add to initial deleted
            List<T> all = Stream.concat(((TrackedList<T>) from).getRemovedElements().stream(), from.stream())
                    .collect(Collectors.toList());
            return new TrackedList<>(newList, all);
        } else {
            return new TrackedList<>(newList, from);
        }
    }

    @Override
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public T set(int index, T element) {
        T removed = delegate.set(index, element);
        this.removedElements.add(removed);
        return removed;
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
        T removed = delegate.remove(index);
        this.removedElements.add(removed);
        return removed;
    }

    public List<T> getRemovedElements() {
        return this.removedElements;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return Objects.equals(this.delegate, o);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }
}
