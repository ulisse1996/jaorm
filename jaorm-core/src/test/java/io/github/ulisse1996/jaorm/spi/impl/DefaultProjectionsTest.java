package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class DefaultProjectionsTest {

    @Mock private ProjectionDelegate delegate;
    private ProjectionsService service;

    @BeforeEach
    void init() {
        Mockito.when(delegate.getProjectionClass()).then(invocation -> delegate.getClass());
        Mockito.when(delegate.getInstance()).thenReturn(delegate);
        this.service = new DefaultProjections(Collections.singleton(delegate));
    }

    @Test
    void should_return_projection() {
        Map<Class<?>, Supplier<ProjectionDelegate>> map = this.service.getProjections();

        Assertions.assertEquals(
                delegate.getProjectionClass(),
                map.keySet().toArray()[0]
        );
        Assertions.assertEquals(
                delegate,
                map.get(delegate.getProjectionClass()).get()
        );
    }
}
