package io.github.ulisse1996.spi;

import io.github.ulisse1996.ServiceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

class DslServiceTest {

    @BeforeEach
    public void resetInstance() {
        DslService.INSTANCE.set(null);
    }

    @Test
    void should_return_no_op() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(DslService.class))
                    .thenReturn(Collections.emptyList());
            DslService instance = DslService.getInstance();
            Assertions.assertTrue(instance instanceof DslService.NoOpModification);
            Assertions.assertFalse(instance.isSupported());
        }
    }

    @Test
    void should_return_dsl_service() {
        DslService service = Mockito.mock(DslService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(DslService.class))
                    .thenReturn(Collections.singletonList(service));

            Mockito.when(service.isSupported())
                    .thenReturn(true);

            DslService found = DslService.getInstance();
            Assertions.assertEquals(service, found);
            Assertions.assertTrue(found.isSupported());
        }
    }
}
