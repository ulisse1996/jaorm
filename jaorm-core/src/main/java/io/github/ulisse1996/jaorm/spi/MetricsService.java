package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.metrics.MetricInfo;
import io.github.ulisse1996.jaorm.metrics.MetricsTracker;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MetricsService {

    private static final Singleton<MetricsTracker> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    private MetricsService() {}

    public static MetricsTracker getInstance() {
        LOCK.lock();
        try {
            BeanProvider provider = BeanProvider.getInstance();

            if (provider.isActive()) {
                return provider.getOptBean(MetricsTracker.class)
                        .orElse(MetricsService.NoOp.INSTANCE);
            }

            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(Set.of(INSTANCE.get().getClass()))) {
                Iterable<MetricsTracker> trackers = ServiceFinder.loadServices(MetricsTracker.class);
                if (trackers.iterator().hasNext()) {
                    INSTANCE.set(trackers.iterator().next());
                } else {
                    INSTANCE.set(NoOp.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    static class NoOp implements MetricsTracker {

        static final NoOp INSTANCE = new NoOp();

        @Override
        public void trackExecution(MetricInfo info) {
            // No Op implementation
        }
    }
}
