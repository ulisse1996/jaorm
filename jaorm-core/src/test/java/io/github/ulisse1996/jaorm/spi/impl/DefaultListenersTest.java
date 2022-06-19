package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.spi.ListenersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class DefaultListenersTest {

    private ListenersService service;

    @BeforeEach
    void init() {
        this.service = new DefaultListeners(Collections.emptyList());
    }

    @Test
    void should_return_empty_event_classes() {
        Assertions.assertEquals(Collections.emptySet(), service.getEventClasses());
    }
}
