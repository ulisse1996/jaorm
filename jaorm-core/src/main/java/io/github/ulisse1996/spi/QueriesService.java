package io.github.ulisse1996.spi;

import io.github.ulisse1996.ServiceFinder;
import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.DaoImplementation;
import io.github.ulisse1996.entity.EntityDelegate;
import io.github.ulisse1996.spi.common.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class QueriesService {

    private static final Singleton<QueriesService> INSTANCE = Singleton.instance();

    public static QueriesService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(ServiceFinder.loadService(QueriesService.class));
        }

        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <T> T getQuery(Class<T> klass) {
        return (T) Optional.ofNullable(getQueries().get(klass))
                .map(DaoImplementation::getDaoSupplier)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Can't find Query for " + klass));
    }

    @SuppressWarnings("unchecked")
    public <T> BaseDao<T> getBaseDao(Class<T> klass) {
        if (isDelegateClass(klass)) {
            klass = (Class<T>) DelegatesService.getInstance().getEntityClass(klass);
        }

        Class<T> finalKlass = klass;
        return (BaseDao<T>) getQueries()
                .values()
                .stream()
                .filter(entry -> entry.getEntityClass().equals(finalKlass))
                .findFirst()
                .map(DaoImplementation::getDaoSupplier)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Can't find BaseDao for " + finalKlass));
    }

    protected <T> boolean isDelegateClass(Class<T> klass) {
        return EntityDelegate.class.isAssignableFrom(klass);
    }

    protected abstract Map<Class<?>, DaoImplementation> getQueries(); //NOSONAR
}
