package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public abstract class FeatureConfigurator {

    private static final Singleton<FeatureConfigurator> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static FeatureConfigurator getInstance() {
        LOCK.lock();
        try {
            BeanProvider provider = BeanProvider.getInstance();

            if (provider.isActive()) {
                return provider.getOptBean(FeatureConfigurator.class)
                        .orElse(DefaultConfiguration.INSTANCE);
            }

            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(Collections.singleton(INSTANCE.get().getClass()))) {
                try {
                    INSTANCE.set(ServiceFinder.loadService(FeatureConfigurator.class));
                } catch (Exception ex) {
                    INSTANCE.set(DefaultConfiguration.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public abstract boolean isInsertAfterFailedUpdateEnabled();

    protected static class DefaultConfiguration extends FeatureConfigurator {

        private static final DefaultConfiguration INSTANCE = new DefaultConfiguration();

        @Override
        public boolean isInsertAfterFailedUpdateEnabled() {
            return true;
        }
    }
}
