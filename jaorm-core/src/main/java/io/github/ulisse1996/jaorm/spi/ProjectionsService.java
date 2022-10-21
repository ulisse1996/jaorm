package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultProjections;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.Map;
import java.util.function.Supplier;

public abstract class ProjectionsService {

    private static final Singleton<ProjectionsService> INSTANCE = Singleton.instance();

    public static synchronized ProjectionsService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(new DefaultProjections(ServiceFinder.loadServices(ProjectionDelegate.class)));
        }
        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <R extends ProjectionDelegate> Supplier<R> searchDelegate(Class<?> entity) {
        return (Supplier<R>) getProjections()
                .entrySet()
                .stream()
                .filter(del -> ClassChecker.isAssignable(del.getKey(), entity))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find projection for " + entity));
    }

    public abstract Map<Class<?>, Supplier<ProjectionDelegate>> getProjections();
}
