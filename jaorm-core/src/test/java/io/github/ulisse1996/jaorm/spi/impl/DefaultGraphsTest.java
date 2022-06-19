package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.spi.GraphsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class DefaultGraphsTest {

    private GraphsService graphsService;

    @BeforeEach
    void init() {
        this.graphsService = new DefaultGraphs(Collections.emptyList());
    }

    @Test
    void should_return_empty_graphs() {
        Assertions.assertEquals(Collections.emptyMap(), graphsService.getEntityGraphs());
    }
}
