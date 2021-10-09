package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class PostgreLimitOffsetSpecific implements LimitOffsetSpecific {

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" LIMIT %d", limitRow);
    }

    @Override
    public String convertOffsetSupport(int offset) {
        return String.format(" OFFSET %d ", offset);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
    }
}
