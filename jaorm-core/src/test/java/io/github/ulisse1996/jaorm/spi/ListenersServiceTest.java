package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
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
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ListenersServiceTest {

    @Mock private ListenersService mock;
    @Mock private GlobalEventListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        try {
            Field instance = ListenersService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<ListenersService> singleton = (Singleton<ListenersService>) instance.get(null);
            singleton.set(null);

            Field globalInstance = ListenersService.class.getDeclaredField("INSTANCE");
            globalInstance.setAccessible(true);
            Singleton<ListenersService> globalSingleton = (Singleton<ListenersService>) globalInstance.get(null);
            globalSingleton.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_no_op_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(ListenersService.class))
                    .thenThrow(IllegalArgumentException.class);
            ListenersService instance = ListenersService.getInstance();
            Assertions.assertTrue(instance.getClass().getName().contains("NoOp"));
            Assertions.assertEquals(Collections.emptySet(), instance.getEventClasses());
        }
    }

    @Test
    void should_handle_event() {
        ListenersService service = new ListenersService() {
            @Override
            protected Set<Class<?>> getEventClasses() {
                return Collections.singleton(Object.class);
            }
        };
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(GlobalEventListener.class))
                    .thenReturn(listener);
            mk.when(() -> ServiceFinder.loadService(ListenersService.class))
                    .thenReturn(service);
            service.fireEvent(new Object(), GlobalEventType.POST_PERSIST);
            Mockito.verify(listener).handleEvent(Mockito.any(), Mockito.any());
        }
    }

    @Test
    void should_return_saved_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(ListenersService.class))
                    .thenReturn(mock);
            mk.when(() -> ServiceFinder.loadService(GlobalEventListener.class))
                    .thenReturn(listener);
            Assertions.assertEquals(mock, ListenersService.getInstance()); // Loaded
            Assertions.assertEquals(mock, ListenersService.getInstance());
            mk.verify(() -> ServiceFinder.loadService(ListenersService.class));
        }
    }
}
