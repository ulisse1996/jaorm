package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.metrics.MetricInfo;
import io.github.ulisse1996.jaorm.metrics.MetricsTracker;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Set;

public abstract class MetricsService {

    private static final Singleton<MetricsTracker> INSTANCE = Singleton.instance();

    public static synchronized MetricsTracker getInstance() {
        BeanProvider provider = BeanProvider.getInstance();

        if (provider.isActive()) {
            return provider.getOptBean(MetricsTracker.class)
                    .orElse(MetricsService.NoOp.INSTANCE);
        }

        if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(Set.of(INSTANCE.get().getClass()))) {
            Iterable<MetricsTracker> trackers = ServiceFinder.loadServices(MetricsTracker.class);
            if (trackers.iterator().hasNext()) {
                INSTANCE.set(trackers.iterator().next());
            }
        }

        return INSTANCE.get();
    }

    static class NoOp implements MetricsTracker {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public void trackExecution(MetricInfo info) {}
    }
}
