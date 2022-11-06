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
class GlobalEventListenerTest {

    @Mock private GlobalEventListener mock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        try {
            Field instance = GlobalEventListener.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<GlobalEventListener> singleton = (Singleton<GlobalEventListener>) instance.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_same_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(GlobalEventListener.class))
                    .thenReturn(mock);
            Assertions.assertEquals(mock, GlobalEventListener.getInstance());
            Assertions.assertEquals(mock, GlobalEventListener.getInstance());
            mk.verify(() -> ServiceFinder.loadService(GlobalEventListener.class));
        }
    }

    @Test
    void should_return_no_op() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(GlobalEventListener.class))
                    .thenThrow(IllegalArgumentException.class);
            GlobalEventListener instance = GlobalEventListener.getInstance();
            Assertions.assertTrue(instance.getClass().getName().contains("NoOp"));
            Assertions.assertThrows(UnsupportedOperationException.class, () -> instance.handleEvent(null, null));
        }
    }

    @Test
    void should_return_bean_provider_global_events_listener() throws NoSuchFieldException, IllegalAccessException {
        Singleton<BeanProvider> singleton = MockedProvider.getSingleton();
        Mockito.when(singleton.get().isActive()).thenReturn(true);
        Mockito.when(singleton.get().getOptBean(GlobalEventListener.class)).thenReturn(Optional.of(mock));
        Assertions.assertEquals(mock, GlobalEventListener.getInstance());
    }
}
