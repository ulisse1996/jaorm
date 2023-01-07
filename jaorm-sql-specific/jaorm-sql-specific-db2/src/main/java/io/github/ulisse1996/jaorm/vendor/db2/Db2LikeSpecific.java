package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class Db2LikeSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeSpecific.LikeType type, boolean caseInsensitiveLike) {
        switch (type) {
            case FULL:
                return caseInsensitiveLike ? " '%' || UPPER(?) || '%'" : " '%' || ? || '%'";
            case START:
                return caseInsensitiveLike ? " '%' || UPPER(?) " : " '%' || ? ";
            case END:
                return caseInsensitiveLike ? " UPPER(?) || '%'" : " ? || '%'";
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
