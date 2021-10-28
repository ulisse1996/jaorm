package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class CombinedProjectionsTest {

    @Mock private ProjectionsService mock1;
    @Mock private ProjectionsService mock2;
    private CombinedProjections combinedProjections;

    @BeforeEach
    public void init() {
        this.combinedProjections = new CombinedProjections(Arrays.asList(mock1, mock2));
    }

    @Test
    void should_return_all_projections() {
        Mockito.when(mock1.getProjections())
                .thenReturn(Collections.singletonMap(Object.class, () -> Mockito.mock(ProjectionDelegate.class)));
        Mockito.when(mock2.getProjections())
                .thenReturn(Collections.singletonMap(String.class, () -> Mockito.mock(ProjectionDelegate.class)));
        Map<Class<?>, Supplier<ProjectionDelegate>> projections = combinedProjections.getProjections();
        Assertions.assertEquals(2, projections.keySet().size());
    }
}
