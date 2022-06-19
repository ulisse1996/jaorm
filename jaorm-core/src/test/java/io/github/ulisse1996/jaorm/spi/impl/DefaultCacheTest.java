package io.github.ulisse1996.jaorm.spi.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class DefaultCacheTest {

    private DefaultCache cache;

    @BeforeEach
    void init() {
        cache = new DefaultCache(Collections.emptyList());
    }

    @Test
    void should_return_false_for_cacheable() {
        Assertions.assertFalse(cache.isCacheable(Object.class));
    }

    @Test
    void should_return_empty_map() {
        Assertions.assertEquals(Collections.emptyMap(), cache.getCaches());
    }
}
