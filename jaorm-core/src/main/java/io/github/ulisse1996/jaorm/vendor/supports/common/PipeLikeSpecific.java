package io.github.ulisse1996.jaorm.vendor.supports.common;

import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class PipeLikeSpecific implements LikeSpecific {

    public static final PipeLikeSpecific INSTANCE = new PipeLikeSpecific();

    private PipeLikeSpecific() {}

    @Override
    public String convertToLikeSupport(LikeSpecific.LikeType type) {
        switch (type) {
            case FULL:
                return "'%' || ? || '%'";
            case START:
                return "'%' || ? ";
            case END:
                return " ? || '%'";
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
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
