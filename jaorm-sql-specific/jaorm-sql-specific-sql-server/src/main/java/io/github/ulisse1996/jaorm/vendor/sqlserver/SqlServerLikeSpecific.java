package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public class SqlServerLikeSpecific implements LikeSpecific {

    @Override
    public String convertToLikeSupport(LikeType type) {
        switch (type) {
            case FULL:
                return " CONCAT('%',?,'%')";
            case START:
                return " CONCAT('%',?)";
            case END:
                return " CONCAT(?,'%')";
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
