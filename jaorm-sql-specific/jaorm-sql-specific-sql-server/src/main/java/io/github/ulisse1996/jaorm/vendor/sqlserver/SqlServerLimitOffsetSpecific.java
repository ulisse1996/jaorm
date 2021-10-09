package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class SqlServerLimitOffsetSpecific implements LimitOffsetSpecific {

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" FETCH NEXT %d ROWS ONLY", limitRow);
    }

    @Override
    public String convertOffsetSupport(int offset) {
        return String.format(" OFFSET %d ROWS ", offset);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", offsetRow, limitRow);
    }
}
