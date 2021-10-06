package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class PostgreLikeSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeType type, boolean caseInsensitiveLike) {
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
