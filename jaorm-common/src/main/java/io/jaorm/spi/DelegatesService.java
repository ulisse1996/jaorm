package io.jaorm.spi;

import io.jaorm.Arguments;
import io.jaorm.ServiceFinder;
import io.jaorm.entity.EntityDelegate;

import java.util.Map;
import java.util.function.Supplier;

public abstract class DelegatesService {

    public static DelegatesService getInstance() {
        return ServiceFinder.loadService(DelegatesService.class);
    }

    @SuppressWarnings("unchecked")
    public <R extends EntityDelegate<?>, T> Supplier<R> searchDelegate(T entity) {
        if (EntityDelegate.class.isAssignableFrom(entity.getClass())) {
            return () -> (R) entity; // We have entity delegate as input
        }

        return searchDelegate(entity.getClass());
    }

    public Class<?> getEntityClass(Class<?> delegateClass) {
        try {
            // Fast exit , search from class loader
            return Class.forName(delegateClass.getName().replace("Delegate", ""));
        } catch (ClassNotFoundException ex) {
            return getDelegates().entrySet()
                    .stream()
                    .filter(e -> {
                        Supplier<?> supplier = e.getValue();
                        return delegateClass.isInstance(supplier.get());
                    }).findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new IllegalArgumentException("Can't find real class from delegate " + delegateClass));
        }
    }

    @SuppressWarnings("unchecked")
    public <R extends EntityDelegate<?>> Supplier<R> searchDelegate(Class<?> entity) {
        return (Supplier<R>) getDelegates().entrySet().stream().filter(del -> !del.getKey().equals(Object.class) && del.getKey().isAssignableFrom(entity))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find delegate for " + entity));
    }

    @SuppressWarnings("unchecked")
    public <R> Arguments asWhere(R entity) {
        EntityDelegate<R> entityDelegate = (EntityDelegate<R>) searchDelegate(entity).get();
        return entityDelegate.getEntityMapper().getKeys(entity);
    }

    @SuppressWarnings("unchecked")
    public <R> Arguments asArguments(R entity) {
        EntityDelegate<R> entityDelegate = (EntityDelegate<R>) searchDelegate(entity).get();
        return entityDelegate.getEntityMapper().getAllColumns(entity);
    }

    public String getSql(Class<?> klass) {
        EntityDelegate<?> delegate = searchDelegate(klass).get();
        return delegate.getBaseSql() + delegate.getKeysWhere();
    }

    public String getSimpleSql(Class<?> klass) {
        return searchDelegate(klass).get().getBaseSql();
    }

    public <R> String getInsertSql(R entity) {
        EntityDelegate<?> delegate = searchDelegate(entity.getClass()).get();
        return delegate.getInsertSql();
    }

    @SuppressWarnings("unchecked")
    public <R> Arguments asInsert(R entity) {
        EntityDelegate<R> delegate = (EntityDelegate<R>) searchDelegate(entity.getClass()).get();
        return delegate.getEntityMapper().getAllColumns(entity);
    }

    public <R> String getUpdateSql(Class<R> entity) {
        EntityDelegate<?> delegate = searchDelegate(entity).get();
        return delegate.getUpdateSql() + delegate.getKeysWhere();
    }

    public <R> String getDeleteSql(Class<R> entity) {
        EntityDelegate<?> delegate = searchDelegate(entity).get();
        return delegate.getDeleteSql();
    }

    protected abstract Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates(); //NOSONAR
}
