package io.jaorm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.ServiceLoader;

class ServiceFinderTest {

    @SuppressWarnings("rawtypes")
    @Test
    void should_not_find_services() {
        ServiceLoader<?> loader = Mockito.mock(ServiceLoader.class);
        Mockito.when(loader.iterator())
                .thenReturn(Collections.emptyIterator());
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(Mockito.any(), Mockito.any()))
                    .thenReturn(loader);
            Assertions.assertFalse(ServiceFinder.loadServices(Object.class).iterator().hasNext());
        }
    }
}