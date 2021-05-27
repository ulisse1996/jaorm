package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class SqlServerSpecific implements LimitOffsetSpecific {

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" FETCH NEXT %d ROWS ONLY", limitRow);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", offsetRow, limitRow);
    }

    @Override
    public DriverType getDriverType() {
        return DriverType.MS_SQLSERVER;
    }
}
