package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.supports.common.StandardOffSetLimitSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MysqlSpecificTest {

    private final MysqlSpecific testSubject = new MysqlSpecific();

    @Test
    void should_return_db2_driver_type() {
        Assertions.assertEquals(DriverType.MYSQL, testSubject.getDriverType());
    }

    @Test
    void should_return_standard_limit() {
        Assertions.assertEquals(
                StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(10),
                testSubject.convertOffSetLimitSupport(10)
        );
    }

    @Test
    void should_return_standard_limit_with_offset() {
        Assertions.assertEquals(
                StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(10, 10),
                testSubject.convertOffSetLimitSupport(10, 10)
        );
    }
}
