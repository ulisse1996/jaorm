package io.github.ulisse1996.jaorm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

class ServiceFinderTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void should_not_find_services() {
        ServiceLoader<?> loader = Mockito.mock(ServiceLoader.class);
        Mockito.when(loader.iterator())
                .thenReturn(Collections.emptyIterator());
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(Mockito.any(Class.class), Mockito.any()))
                    .thenReturn(loader);
            Assertions.assertFalse(ServiceFinder.loadServices(Object.class).iterator().hasNext());
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void should_find_one_service() {
        Object expected = new Object();
        List<Object> list = Collections.singletonList(expected);
        ServiceLoader<?> loader = Mockito.mock(ServiceLoader.class);
        Mockito.doReturn(list.iterator())
                .when(loader).iterator();
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(Mockito.any(Class.class), Mockito.any()))
                    .thenReturn(loader);
            Object next = ServiceFinder.loadServices(Object.class).iterator().next();
            Assertions.assertEquals(expected, next);
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void should_throw_exception_for_missing_service() {
        ServiceLoader<?> loader = Mockito.mock(ServiceLoader.class);
        Mockito.when(loader.iterator())
                .thenReturn(Collections.emptyIterator());
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(Mockito.any(Class.class), Mockito.any()))
                    .thenReturn(loader);
            Assertions.assertThrows(IllegalArgumentException.class, () -> ServiceFinder.loadService(Object.class));
        }
    }
}
