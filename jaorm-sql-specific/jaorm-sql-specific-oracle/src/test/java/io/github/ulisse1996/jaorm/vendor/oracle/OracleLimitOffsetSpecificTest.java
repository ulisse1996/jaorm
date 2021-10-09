package io.github.ulisse1996.jaorm.vendor.oracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OracleLimitOffsetSpecificTest {

    private final OracleLimitOffsetSpecific testSubject = new OracleLimitOffsetSpecific();

    @Test
    void should_return_fetch() {
        Assertions.assertEquals(" FETCH FIRST 10 ROWS ONLY", testSubject.convertOffSetLimitSupport(10));
    }

    @Test
    void should_return_fetch_with_offset() {
        Assertions.assertEquals(" OFFSET 10 ROWS FETCH FIRST 10 ROWS ONLY", testSubject.convertOffSetLimitSupport(10, 10));
    }

    @Test
    void should_return_offset() {
        Assertions.assertEquals(" OFFSET 10 ROWS ", testSubject.convertOffsetSupport(10));
    }
}
