package io.jaorm.entity;

import io.jaorm.ServiceFinder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class QueriesService {

    public static QueriesService getInstance() {
        return ServiceFinder.loadService(QueriesService.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getQuery(Class<T> klass) {
        return Optional.ofNullable(getQueries().get(klass))
                .map(s -> (Supplier<? extends T>) s)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Can't find Query for class " + klass));
    }

    protected abstract Map<Class<?>, Supplier<?>> getQueries(); //NOSONAR
}
