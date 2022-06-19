package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultProjections;

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

    public abstract Map<Class<?>, Supplier<ProjectionDelegate>> getProjections();
}
