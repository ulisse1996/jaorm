package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Set;
import java.util.stream.StreamSupport;

public abstract class FrameworkIntegrationService {

    private static final Singleton<FrameworkIntegrationService> INSTANCE = Singleton.instance();

    public static synchronized FrameworkIntegrationService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(
                    StreamSupport.stream(ServiceFinder.loadServices(FrameworkIntegrationService.class).spliterator(), false)
                            .findFirst()
                            .orElse(FrameworkIntegrationService.NoOp.INSTANCE)
            );
        }

        return INSTANCE.get();
    }

    public static boolean isReloadRequired(Set<Class<?>> classes) {
        FrameworkIntegrationService instance = FrameworkIntegrationService.getInstance();
        return instance.isActive() && instance.requireReInit(classes);
    }

    public abstract boolean isActive();
    public abstract boolean requireReInit(Set<Class<?>> classes);

    static class NoOp extends FrameworkIntegrationService {

        private static final FrameworkIntegrationService INSTANCE = new NoOp();

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean requireReInit(Set<Class<?>> classes) {
            return false;
        }
    }
}
