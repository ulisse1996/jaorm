package io.github.ulisse1996.jaorm.vendor.mysql.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentTimeFunctionTest {

    @Test
    void should_return_current_time_function() {
        Assertions.assertEquals("CURRENT_TIME", CurrentTimeFunction.INSTANCE.apply(""));
    }
}
