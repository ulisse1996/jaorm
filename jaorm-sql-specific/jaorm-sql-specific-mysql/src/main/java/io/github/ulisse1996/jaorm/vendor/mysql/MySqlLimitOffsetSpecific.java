package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class MySqlLimitOffsetSpecific implements LimitOffsetSpecific {

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
        return String.format(" LIMIT %d, %d", offsetRow, limitRow);
    }

    @Override
    public boolean requiredOrder() {
        return false;
    }
}
