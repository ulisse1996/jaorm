package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.metrics.MetricInfo;
import io.github.ulisse1996.jaorm.metrics.MetricsTracker;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock private MetricsTracker tracker;
    @Mock private BeanProvider provider;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reset() {
        try {
            Field field = MetricsService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<MetricsTracker> singleton = (Singleton<MetricsTracker>) field.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_get_tracker_from_bean_provider() {
        try (MockedStatic<BeanProvider> mk = Mockito.mockStatic(BeanProvider.class)) {
            mk.when(BeanProvider::getInstance).thenReturn(provider);
            Mockito.when(provider.isActive()).thenReturn(true);
            Mockito.when(provider.getOptBean(MetricsTracker.class))
                    .thenReturn(Optional.of(tracker));
            Assertions.assertEquals(
                    tracker,
                    MetricsService.getInstance()
            );
        }
    }

    @Test
    void should_return_no_op_from_bean_provider() {
        try (MockedStatic<BeanProvider> mk = Mockito.mockStatic(BeanProvider.class)) {
            mk.when(BeanProvider::getInstance).thenReturn(provider);
            Mockito.when(provider.isActive()).thenReturn(true);
            Mockito.when(provider.getOptBean(MetricsTracker.class))
                    .thenReturn(Optional.empty());
            Assertions.assertTrue(
                    MetricsService.getInstance() instanceof MetricsService.NoOp
            );
        }
    }

    @Test
    void should_return_metric_tracker_from_spi() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(MetricsTracker.class))
                    .thenReturn(Collections.singleton(tracker));
            Assertions.assertEquals(
                    tracker,
                    MetricsService.getInstance()
            );
        }
    }

    @Test
    void should_return_no_op_for_empty_services() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(MetricsTracker.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertTrue(
                    MetricsService.getInstance() instanceof MetricsService.NoOp
            );
        }
    }

    @Test
    void should_return_new_instance_after_reload() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<FrameworkIntegrationService> mkFwk = Mockito.mockStatic(FrameworkIntegrationService.class)) {
            mk.when(() -> ServiceFinder.loadServices(MetricsTracker.class))
                    .thenReturn(Collections.emptyList(), Collections.singleton(tracker));
            mkFwk.when(() -> FrameworkIntegrationService.isReloadRequired(Mockito.any()))
                    .thenReturn(true);
            Assertions.assertTrue(
                    MetricsService.getInstance() instanceof MetricsService.NoOp
            );
            Assertions.assertEquals(
                    tracker,
                    MetricsService.getInstance()
            );
        }
    }

    @Test
    void should_do_nothing_for_tracking() {
        Assertions.assertDoesNotThrow(() -> MetricsService.NoOp.INSTANCE.trackExecution(
                MetricInfo.of("", Collections.emptyList(), false, 10)
        ));
    }
}