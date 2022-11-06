package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
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
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class ProjectionsServiceTest {

    @Mock private ProjectionDelegate delegate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void resetProjections() {
        try {
            Field field = ProjectionsService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<ProjectionsService> singleton = (Singleton<ProjectionsService>) field.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_selected_projection() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ProjectionDelegate.class))
                    .thenReturn(Collections.singletonList(delegate));

            Mockito.when(delegate.getProjectionClass()).then(invocationOnMock -> Object.class);
            Mockito.when(delegate.getInstance()).thenReturn(delegate);

            ProjectionsService service = ProjectionsService.getInstance();

            Supplier<ProjectionDelegate> supplier = service.searchDelegate(Object.class);
            Assertions.assertEquals(delegate, supplier.get());
        }
    }

    @Test
    void should_throw_exception_for_missing_delegate() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ProjectionDelegate.class))
                    .thenReturn(Collections.emptyList());
            ProjectionsService service = ProjectionsService.getInstance();

            Assertions.assertThrows(IllegalArgumentException.class, () -> service.searchDelegate(delegate.getClass())); //NOSONAR
        }
    }
}