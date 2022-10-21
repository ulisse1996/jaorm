package io.github.ulisse1996.jaorm.vendor.mysql.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentTimestampFunctionTest {

    @Test
    void should_return_current_timestamp_function() {
        Assertions.assertEquals("CURRENT_TIMESTAMP", CurrentTimestampFunction.INSTANCE.apply(""));
    }

    @Test
    void should_return_false_for_string_function() {
        Assertions.assertFalse(CurrentTimestampFunction.INSTANCE.isString());
    }
}
