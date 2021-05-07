package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.supports.common.PipeLikeSpecific;

public class PostgreSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeType type) {
        return PipeLikeSpecific.INSTANCE.convertToLikeSupport(type);
    }

    @Override
    public DriverType getDriverType() {
        return DriverType.POSTGRE;
    }
}
