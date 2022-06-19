package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultQueries;
import io.github.ulisse1996.jaorm.spi.provider.QueryProvider;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.Map;
import java.util.function.Supplier;

public abstract class QueriesService {

    private static final Singleton<QueriesService> INSTANCE = Singleton.instance();

    public static synchronized QueriesService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(new DefaultQueries(ServiceFinder.loadServices(QueryProvider.class)));
        }

        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <T> T getQuery(Class<T> klass) {
        return (T) getQueries()
                .entrySet()
                .stream()
                .filter(el -> ClassChecker.isAssignable(el.getKey(), klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(DaoImplementation::getDaoSupplier)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Can't find Query for " + klass));
    }

    @SuppressWarnings("unchecked")
    public <T> BaseDao<T> getBaseDao(Class<T> klass) {
        Class<?> found;
        if (isDelegateClass(klass)) {
            found = DelegatesService.getInstance().getEntityClass(klass);
        } else {
            found = klass;
        }

        return (BaseDao<T>) getQueries()
                .values()
                .stream()
                .filter(entry -> ClassChecker.isAssignable(entry.getEntityClass(), found))
                .findFirst()
                .map(DaoImplementation::getDaoSupplier)
                .map(Supplier::get)
                .filter(c -> c instanceof BaseDao<?>)
                .orElseThrow(() -> new IllegalArgumentException("Can't find BaseDao for " + found));
    }

    public <T> boolean isDelegateClass(Class<T> klass) {
        return EntityDelegate.class.isAssignableFrom(klass);
    }

    public abstract Map<Class<?>, DaoImplementation> getQueries(); //NOSONAR
}
