package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.spi.DelegatesService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityComparator<T> {

    private final Class<? extends EntityDelegate<T>> delegate;

    private EntityComparator(Class<? extends EntityDelegate<T>> klass) {
        this.delegate = klass;
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityComparator<T> getInstance(Class<T> klass) {
        return new EntityComparator<>((Class<? extends EntityDelegate<T>>) DelegatesService.getInstance()
                .searchDelegate(klass).get().getClass());
    }

    public static <T> Predicate<T> distinct(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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
