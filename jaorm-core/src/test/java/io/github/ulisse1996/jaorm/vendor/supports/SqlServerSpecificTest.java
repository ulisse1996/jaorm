package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlServerSpecificTest {

    private final SqlServerSpecific testSubject = new SqlServerSpecific();

    @Test
    void should_return_sql_server_driver() {
        Assertions.assertEquals(DriverType.MS_SQLSERVER, testSubject.getDriverType());
    }

    @Test
    void should_return_sql_server_limit() {
        Assertions.assertEquals(" FETCH NEXT 10 ROWS ONLY",
                testSubject.convertOffSetLimitSupport(10));
    }

    @Test
    void should_return_sql_server_limit_with_offset() {
        Assertions.assertEquals(" OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY",
                testSubject.convertOffSetLimitSupport(10, 10));
    }
}
