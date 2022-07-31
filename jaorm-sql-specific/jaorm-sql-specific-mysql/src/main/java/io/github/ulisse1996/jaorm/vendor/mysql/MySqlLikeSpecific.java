package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class MySqlLikeSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeType type, boolean caseInsensitiveLike) {
        switch (type) {
            case FULL:
                return caseInsensitiveLike ? " CONCAT('%',UPPER(?),'%')" : " CONCAT('%',?,'%')";
            case START:
                return caseInsensitiveLike ? " CONCAT('%',UPPER(?))" : " CONCAT('%',?)";
            case END:
                return caseInsensitiveLike ? " CONCAT(UPPER(?),'%')" : " CONCAT(?,'%')";
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
