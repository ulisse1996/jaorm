package io.github.ulisse1996.jaorm.vendor.sqlserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlServerLimitOffsetSpecificTest {

    private final SqlServerLimitOffsetSpecific testSubject = new SqlServerLimitOffsetSpecific();

    @Test
    void should_return_fetch() {
        Assertions.assertEquals(" FETCH NEXT 10 ROWS ONLY", testSubject.convertOffSetLimitSupport(10));
    }

    @Test
    void should_return_fetch_with_offset() {
        Assertions.assertEquals(" OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY", testSubject.convertOffSetLimitSupport(10, 10));
    }

    @Test
    void should_return_offset() {
        Assertions.assertEquals(" OFFSET 10 ROWS ", testSubject.convertOffsetSupport(10));
    }
}
