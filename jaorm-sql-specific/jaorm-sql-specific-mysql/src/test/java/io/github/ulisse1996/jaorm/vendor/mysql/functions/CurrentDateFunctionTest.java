package io.github.ulisse1996.jaorm.vendor.mysql.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrentDateFunctionTest {

    @Test
    void should_return_current_date_function() {
        Assertions.assertEquals("CURRENT_DATE", CurrentDateFunction.INSTANCE.apply(""));
    }

    @Test
    void should_return_false_for_string_function() {
        Assertions.assertFalse(CurrentDateFunction.INSTANCE.isString());
    }
}
