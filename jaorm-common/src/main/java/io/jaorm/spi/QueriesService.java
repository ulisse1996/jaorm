package io.jaorm.spi;

import io.jaorm.BaseDao;
import io.jaorm.DaoImplementation;
import io.jaorm.ServiceFinder;
import io.jaorm.entity.EntityDelegate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class QueriesService {

    public static QueriesService getInstance() {
        return ServiceFinder.loadService(QueriesService.class);
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
