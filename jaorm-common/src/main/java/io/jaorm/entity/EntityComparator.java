package io.jaorm.entity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityComparator<T> {

    private final Class<? extends EntityDelegate<T>> delegate;

    private EntityComparator(Class<? extends EntityDelegate<T>> klass) {
        this.delegate = klass;
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityComparator<T> getInstance(Class<T> klass) {
        return new EntityComparator<>((Class<? extends EntityDelegate<T>>) DelegatesService.getCurrent()
                .searchDelegate(klass).get().getClass());
    }

    public boolean equals(List<T> first, List<T> second) {
        if (first == null) {
            return second == null;
        } else if (second == null) {
            return false;
        }

        return first.stream()
                .map(this::getEntity)
                .collect(Collectors.toList())
                .equals(
                        second.stream()
                            .map(this::getEntity)
                            .collect(Collectors.toList())
                );
    }

    public boolean equals(T first, T second) {
        Object firstEntity = getEntity(first);
        Object secondEntity = getEntity(second);
        if (firstEntity == null) {
            return secondEntity == null;
        }

        return Objects.equals(firstEntity, secondEntity);
    }

    private Object getEntity(T entity) {
        if (entity == null) {
            return null;
        }

        if (delegate.isInstance(entity)) {
            return delegate.cast(entity).getEntity();
        }

        return entity;
    }
}
