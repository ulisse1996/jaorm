package io.github.ulisse1996.jaorm.vendor.db2.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentDateFunctionTest {

    @Test
    void should_return_current_date() {
        Assertions.assertEquals("CURRENT DATE", CurrentDateFunction.INSTANCE.apply(null));
    }

    @Test
    void should_return_false_for_is_string() {
        Assertions.assertFalse(CurrentDateFunction.INSTANCE.isString());
    }
}