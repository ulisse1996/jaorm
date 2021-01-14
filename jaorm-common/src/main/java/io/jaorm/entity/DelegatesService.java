package io.jaorm.entity;

import io.jaorm.Arguments;
import io.jaorm.ServiceFinder;

import java.util.Map;
import java.util.function.Supplier;

public abstract class DelegatesService {

    public static DelegatesService getCurrent() {
        return ServiceFinder.loadService(DelegatesService.class);
    }

    @SuppressWarnings("unchecked")
    public <R extends EntityDelegate<?>> Supplier<R> searchDelegate(Class<?> entity) {
        return (Supplier<R>) getDelegates().entrySet().stream().filter(del -> del.getKey().isAssignableFrom(entity))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find delegate for " + entity));
    }

    @SuppressWarnings("unchecked")
    public <R> Arguments asArguments(R entity) {
        EntityDelegate<R> entityDelegateSupplier = (EntityDelegate<R>) searchDelegate(entity.getClass()).get();
        return entityDelegateSupplier.getEntityMapper().getKeys(entity);
    }

    public String getSql(Class<?> klass) {
        return ""; // TODO
    }

    protected abstract Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates();
}
