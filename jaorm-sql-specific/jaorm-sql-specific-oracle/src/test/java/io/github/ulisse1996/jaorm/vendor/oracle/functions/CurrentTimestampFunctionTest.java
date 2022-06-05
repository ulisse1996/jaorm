package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentTimestampFunctionTest {

    @Test
    void should_return_false_for_is_string() {
        Assertions.assertFalse(CurrentTimestampFunction.INSTANCE.isString());
    }

    @Test
    void should_return_current_timestamp() {
        Assertions.assertEquals("CURRENT_TIMESTAMP", CurrentTimestampFunction.INSTANCE.apply(""));
    }
}
