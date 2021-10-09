package io.github.ulisse1996.jaorm.vendor.postgre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostgreLimitOffsetSpecificTest {

    private final PostgreLimitOffsetSpecific testSubject = new PostgreLimitOffsetSpecific();

    @Test
    void should_return_fetch() {
        Assertions.assertEquals(" LIMIT 10", testSubject.convertOffSetLimitSupport(10));
    }

    @Test
    void should_return_fetch_with_offset() {
        Assertions.assertEquals(" LIMIT 10 OFFSET 10", testSubject.convertOffSetLimitSupport(10, 10));
    }

    @Test
    void should_return_offset() {
        Assertions.assertEquals(" OFFSET 10 ", testSubject.convertOffsetSupport(10));
    }
}
