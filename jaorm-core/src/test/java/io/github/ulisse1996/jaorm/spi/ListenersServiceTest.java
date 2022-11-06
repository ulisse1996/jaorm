package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.DelegatesMock;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultListeners;
import io.github.ulisse1996.jaorm.spi.provider.ListenerProvider;
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

    @Mock private BeanProvider provider;
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

            Field globalInstance = GlobalEventListener.class.getDeclaredField("INSTANCE");
            globalInstance.setAccessible(true);
            Singleton<GlobalEventListener> globalSingleton = (Singleton<GlobalEventListener>) globalInstance.get(null);
            globalSingleton.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_no_op_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ListenerProvider.class))
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
            public Set<Class<?>> getEventClasses() {
                return Collections.singleton(Object.class);
            }
        };
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<BeanProvider> mkProvider = Mockito.mockStatic(BeanProvider.class)) {
            mkProvider.when(BeanProvider::getInstance).thenReturn(provider);
            mk.when(() -> ServiceFinder.loadService(GlobalEventListener.class))
                    .thenReturn(listener);
            mk.when(() -> ServiceFinder.loadService(ListenersService.class))
                    .thenReturn(service);
            service.fireEvent(new Object(), GlobalEventType.POST_PERSIST);
            Mockito.verify(listener).handleEvent(Mockito.any(), Mockito.any());
        }
    }

    @Test
    void should_fire_event_with_delegate() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            mk.when(() -> ServiceFinder.loadServices(ListenersService.class))
                    .thenReturn(Collections.nCopies(3, mock));
            ListenersService.getInstance().fireEvent(new DelegatesMock.MyEntityDelegate(), GlobalEventType.POST_PERSIST);
            Mockito.verify(delegatesService)
                    .getEntityClass(DelegatesMock.MyEntityDelegate.class);
        }
    }

    @Test
    void should_return_default_impl() {
        ListenerProvider provider = Mockito.mock(ListenerProvider.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ListenerProvider.class))
                    .thenReturn(Collections.singletonList(provider));

            Mockito.when(provider.getEntityClass()).then(invocation -> Object.class);

            ListenersService service = ListenersService.getInstance();

            Assertions.assertTrue(service instanceof DefaultListeners);
        }
    }
}
