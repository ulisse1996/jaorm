package io.github.ulisse1996.jaorm.vendor.db2.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentTimeFunctionTest {

    @Test
    void should_return_current_time() {
        Assertions.assertEquals("CURRENT TIME", CurrentTimeFunction.INSTANCE.apply(null));
    }

    @Test
    void should_return_false_for_is_string() {
        Assertions.assertFalse(CurrentTimeFunction.INSTANCE.isString());
    }
}