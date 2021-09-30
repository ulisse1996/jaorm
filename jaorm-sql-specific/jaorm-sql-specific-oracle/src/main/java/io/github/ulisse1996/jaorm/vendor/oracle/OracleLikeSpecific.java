package io.github.ulisse1996.jaorm.vendor.oracle;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class OracleLikeSpecific implements LikeSpecific {

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
}
