package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import io.github.ulisse1996.jaorm.vendor.supports.common.PipeLikeSpecific;

public class OracleSpecific implements LikeSpecific, LimitOffsetSpecific {

    @Override
    public String convertToLikeSupport(LikeType type) {
        return PipeLikeSpecific.INSTANCE.convertToLikeSupport(type);
    }

    @Override
    public DriverType getDriverType() {
        return DriverType.ORACLE;
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow) {
        return String.format(" FETCH FIRST %d ROWS ONLY", limitRow);
    }

    @Override
    public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
        return String.format(" OFFSET %d FETCH FIRST %d ROWS ONLY", offsetRow, limitRow);
    }
}
