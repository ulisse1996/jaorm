package io.github.ulisse1996.jaorm.vendor.supports.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StandardOffSetLimitSpecificTest {

    @Test
    void should_throw_unsupported_exception() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                StandardOffSetLimitSpecific.INSTANCE::supportSpecific);
    }

    @Test
    void should_return_null_driver() {
        Assertions.assertNull(StandardOffSetLimitSpecific.INSTANCE.getDriverType());
    }
}
