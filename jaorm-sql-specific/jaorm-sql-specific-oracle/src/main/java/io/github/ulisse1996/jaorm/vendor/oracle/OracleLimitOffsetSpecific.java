package io.github.ulisse1996.jaorm.vendor.oracle;

import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public class OracleLimitOffsetSpecific implements LimitOffsetSpecific {

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" FETCH FIRST %d ROWS ONLY", limitRow);
    }

    @Override
    public String convertOffsetSupport(int offset) {
        return String.format(" OFFSET %d ROWS ", offset);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offsetRow, limitRow);
    }
}
