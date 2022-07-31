package io.github.ulisse1996.jaorm.vendor.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MySqlLimitOffsetSpecificTest {

    private final MySqlLimitOffsetSpecific specific = new MySqlLimitOffsetSpecific();

    @Test
    void should_return_limit_string() {
        Assertions.assertEquals(
                " LIMIT 10",
                specific.convertOffSetLimitSupport(10)
        );
    }

    @Test
    void should_return_offset_string() {
        Assertions.assertEquals(
                " OFFSET 10 ",
                specific.convertOffsetSupport(10)
        );
    }

    @Test
    void should_return_limit_offset_string() {
        Assertions.assertEquals(
                " LIMIT 15, 10",
                specific.convertOffSetLimitSupport(10, 15)
        );
    }

    @Test
    void should_return_false_for_required_order() {
        Assertions.assertFalse(specific.requiredOrder());
    }
}
