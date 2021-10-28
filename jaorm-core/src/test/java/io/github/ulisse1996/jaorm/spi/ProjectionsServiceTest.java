package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.combined.CombinedProjections;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

class ProjectionsServiceTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        try {
            Field field = ProjectionsService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<ProjectionsService> instance = (Singleton<ProjectionsService>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_only_1_service() {
        ProjectionsService mock = Mockito.mock(ProjectionsService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ProjectionsService.class))
                    .thenReturn(Collections.singletonList(mock));
            Assertions.assertEquals(mock, ProjectionsService.getInstance());
        }
    }

    @Test
    void should_return_combined_service() {
        ProjectionsService mock = Mockito.mock(ProjectionsService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ProjectionsService.class))
                    .thenReturn(Collections.nCopies(3, mock));
            Assertions.assertTrue(ProjectionsService.getInstance() instanceof CombinedProjections);
        }
    }
}
