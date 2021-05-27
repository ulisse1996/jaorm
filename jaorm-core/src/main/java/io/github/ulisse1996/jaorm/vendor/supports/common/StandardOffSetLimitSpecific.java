package io.github.ulisse1996.jaorm.vendor.supports.common;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class StandardOffSetLimitSpecific implements LimitOffsetSpecific {

    public static final StandardOffSetLimitSpecific INSTANCE = new StandardOffSetLimitSpecific();

    private StandardOffSetLimitSpecific() {}

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" LIMIT %d", limitRow);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
    }

    @Override
    public boolean supportSpecific() {
        throw new UnsupportedOperationException("Please don't use common implementation for support checks");
    }

    @Override
    public DriverType getDriverType() {
        return null;
    }
}
