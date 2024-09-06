package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultProjections;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class ProjectionsService {

    private static final Singleton<ProjectionsService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static ProjectionsService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(INSTANCE.get().getProjections().keySet())) {
                INSTANCE.set(new DefaultProjections(ServiceFinder.loadServices(ProjectionDelegate.class)));
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <R extends ProjectionDelegate> Supplier<R> searchDelegate(Class<?> entity) {
        return (Supplier<R>) getProjections()
                .entrySet()
                .stream()
                .filter(del -> del.getKey().equals(entity))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find projection for " + entity));
    }

    public abstract Map<Class<?>, Supplier<ProjectionDelegate>> getProjections();
}
