package io.github.ulisse1996.jaorm.vendor.supports.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PipeLikeSpecificTest {

    @Test
    void should_throw_unsupported_for_check_specific() {
        Assertions.assertThrows(UnsupportedOperationException.class, PipeLikeSpecific.INSTANCE::supportSpecific);
    }

    @Test
    void should_return_null_for_driver_name() {
        Assertions.assertNull(PipeLikeSpecific.INSTANCE.getDriverType());
    }
}
