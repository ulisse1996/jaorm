package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2LimitOffsetSpecificTest {

    private final Db2LimitOffsetSpecific specific = new Db2LimitOffsetSpecific();

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
                " LIMIT 10 OFFSET 15",
                specific.convertOffSetLimitSupport(10, 15)
        );
    }

    @Test
    void should_return_false_for_required_order() {
        Assertions.assertFalse(specific.requiredOrder());
    }
}