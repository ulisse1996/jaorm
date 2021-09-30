package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class PostgreLikeSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeType type) {
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
