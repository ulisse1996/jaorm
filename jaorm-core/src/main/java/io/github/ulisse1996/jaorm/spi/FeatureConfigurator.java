package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

public abstract class FeatureConfigurator {

    private static final Singleton<FeatureConfigurator> INSTANCE = Singleton.instance();

    public static synchronized FeatureConfigurator getInstance() {
        BeanProvider provider = BeanProvider.getInstance();

        if (provider.isActive()) {
            return provider.getOptBean(FeatureConfigurator.class)
                    .orElse(DefaultConfiguration.INSTANCE);
        }

        if (!INSTANCE.isPresent()) {
            try {
                INSTANCE.set(ServiceFinder.loadService(FeatureConfigurator.class));
            } catch (Exception ex) {
                INSTANCE.set(DefaultConfiguration.INSTANCE);
            }
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
