package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.spi.ConverterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class DefaultConvertersTest {

    private ConverterService service;

    @BeforeEach
    void init() {
        service = new DefaultConverters(Collections.emptyList());
    }

    @Test
    void should_return_empty_converters() {
        Assertions.assertEquals(Collections.emptyMap(), service.getConverters());
    }
}
