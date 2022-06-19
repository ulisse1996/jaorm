package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
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
class DefaultDelegatesTest {

    @Mock private EntityDelegate<?> delegate;
    private DelegatesService service;

    @BeforeEach
    void init() {
        Mockito.when(delegate.getEntityInstance())
                .then(invocation -> (Supplier<Object>) Object::new);
        this.service = new DefaultDelegates(Collections.singletonList(delegate));
    }

    @Test
    void should_return_delegate_map() {
        Map<Class<?>, Supplier<? extends EntityDelegate<?>>> map = service.getDelegates();
        Assertions.assertEquals(2, map.keySet().size());
    }
}
