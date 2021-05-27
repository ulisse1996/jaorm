package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import io.github.ulisse1996.jaorm.vendor.supports.common.StandardOffSetLimitSpecific;

public class Db2Specific implements LimitOffsetSpecific {

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(limitRow);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(limitRow, offsetRow);
    }

    @Override
    public DriverType getDriverType() {
        return DriverType.DB2;
    }
}
