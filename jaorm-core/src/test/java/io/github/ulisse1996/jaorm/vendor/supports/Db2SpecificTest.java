package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.supports.common.StandardOffSetLimitSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2SpecificTest {

    private final Db2Specific testSubject = new Db2Specific();

    @Test
    void should_return_db2_driver_type() {
        Assertions.assertEquals(DriverType.DB2, testSubject.getDriverType());
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
