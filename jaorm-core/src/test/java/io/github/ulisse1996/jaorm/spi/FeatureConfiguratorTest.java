package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.MockedProvider;
import io.github.ulisse1996.jaorm.ServiceFinder;
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
import java.util.Optional;

@ExtendWith({MockitoExtension.class, MockedProvider.class})
class FeatureConfiguratorTest {

    @Mock private FeatureConfigurator mock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        try {
            Field instance = FeatureConfigurator.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<FeatureConfigurator> singleton = (Singleton<FeatureConfigurator>) instance.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_same_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(FeatureConfigurator.class))
                    .thenReturn(mock);
            Assertions.assertEquals(mock, FeatureConfigurator.getInstance());
            Assertions.assertEquals(mock, FeatureConfigurator.getInstance());
            mk.verify(() -> ServiceFinder.loadService(FeatureConfigurator.class));
        }
    }

    @Test
    void should_return_default_configuration() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(FeatureConfigurator.class))
                    .thenThrow(IllegalArgumentException.class);
            Assertions.assertEquals(FeatureConfigurator.getInstance().getClass(), FeatureConfigurator.DefaultConfiguration.class);
        }
    }

    @Test
    void should_return_true_for_insert_after_failed_update_for_default_configuration() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(FeatureConfigurator.class))
                    .thenThrow(IllegalArgumentException.class);
            FeatureConfigurator instance = FeatureConfigurator.getInstance();
            Assertions.assertTrue(instance.isInsertAfterFailedUpdateEnabled());
        }
    }

    @Test
    void should_return_bean_provider_feature_configurator() throws NoSuchFieldException, IllegalAccessException {
        Singleton<BeanProvider> singleton = MockedProvider.getSingleton();
        Mockito.when(singleton.get().isActive()).thenReturn(true);
        Mockito.when(singleton.get().getOptBean(FeatureConfigurator.class)).thenReturn(Optional.of(mock));

        Assertions.assertEquals(mock, FeatureConfigurator.getInstance());
    }
}
