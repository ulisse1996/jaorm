package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.combined.CombinedDelegates;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class DelegatesService {

    private static final Singleton<DelegatesService> INSTANCE = Singleton.instance();

    public static synchronized DelegatesService getInstance() {
        if (!INSTANCE.isPresent()) {
            Iterable<DelegatesService> delegatesServices = ServiceFinder.loadServices(DelegatesService.class);
            if (!delegatesServices.iterator().hasNext()) {
                throw new IllegalArgumentException("Can't find service of " + DelegatesService.class);
            }
            List<DelegatesService> delegates = StreamSupport.stream(delegatesServices.spliterator(), false)
                    .collect(Collectors.toList());

            if (delegates.size() == 1) {
                INSTANCE.set(delegates.get(0));
            } else {
                INSTANCE.set(new CombinedDelegates(delegates));
            }
        }

        return INSTANCE.get();
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
        return entityDelegate.getEntityMapper().getAllColumns(entity, false);
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
        return delegate.getEntityMapper().getAllColumns(entity, true);
    }

    @SuppressWarnings("unchecked")
    public <R> Arguments asInsert(R entity, Map<String, Object> generated) {
        EntityDelegate<R> delegate = (EntityDelegate<R>) searchDelegate(entity.getClass()).get();
        return delegate.getEntityMapper().getAllColumns(entity, true, generated);
    }

    public <R> String getUpdateSql(Class<R> entity) {
        EntityDelegate<?> delegate = searchDelegate(entity).get();
        return delegate.getUpdateSql() + delegate.getKeysWhere();
    }

    public <R> String getDeleteSql(Class<R> entity) {
        EntityDelegate<?> delegate = searchDelegate(entity).get();
        return delegate.getDeleteSql();
    }

    public abstract Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates(); //NOSONAR
}
