package io.github.ulisse1996.dsl.generator;

import io.github.ulisse1996.spi.DslService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DslColumnSupportTest {

    @BeforeEach
    public void resetInstance() {
        DslService.INSTANCE.set(null);
    }

    @Test
    void should_return_true_for_support() {
        Assertions.assertTrue(DslService.getInstance().isSupported());
    }
}
