package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.combined.CombinedProjections;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class ProjectionsService {

    private static final Singleton<ProjectionsService> INSTANCE = Singleton.instance();

    public static synchronized ProjectionsService getInstance() {
        if (!INSTANCE.isPresent()) {
            List<ProjectionsService> services = StreamSupport.stream(
                    ServiceFinder.loadServices(ProjectionsService.class).spliterator(), false)
                    .collect(Collectors.toList());
            if (services.size() == 1) {
                INSTANCE.set(services.get(0));
            } else {
                INSTANCE.set(new CombinedProjections(services));
            }
        }
        return INSTANCE.get();
    }

    public abstract Map<Class<?>, Supplier<ProjectionDelegate>> getProjections();
}
