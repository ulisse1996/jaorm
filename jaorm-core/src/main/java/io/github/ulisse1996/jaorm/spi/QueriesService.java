package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.combined.CombinedQueries;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class QueriesService {

    private static final Singleton<QueriesService> INSTANCE = Singleton.instance();

    public static synchronized QueriesService getInstance() {
        if (!INSTANCE.isPresent()) {
            List<QueriesService> services = StreamSupport.stream(ServiceFinder.loadServices(QueriesService.class).spliterator(), false)
                            .collect(Collectors.toList());
            if (services.size() == 1) {
                INSTANCE.set(services.get(0));
            } else {
                INSTANCE.set(new CombinedQueries(services));
            }
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
        Class<?> found;
        if (isDelegateClass(klass)) {
            found = DelegatesService.getInstance().getEntityClass(klass);
        } else {
            found = klass;
        }

        return (BaseDao<T>) getQueries()
                .values()
                .stream()
                .filter(entry -> entry.getEntityClass().equals(found))
                .findFirst()
                .map(DaoImplementation::getDaoSupplier)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Can't find BaseDao for " + found));
    }

    public <T> boolean isDelegateClass(Class<T> klass) {
        return EntityDelegate.class.isAssignableFrom(klass);
    }

    public abstract Map<Class<?>, DaoImplementation> getQueries(); //NOSONAR
}
