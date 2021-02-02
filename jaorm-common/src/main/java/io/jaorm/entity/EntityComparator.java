package io.jaorm.entity;

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

    public boolean equals(T first, T second) {
        Object firstEntity = getEntity(first);
        Object secondEntity = getEntity(second);
        if (firstEntity == null) {
            return secondEntity == null;
        }
        return firstEntity.equals(secondEntity);
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
