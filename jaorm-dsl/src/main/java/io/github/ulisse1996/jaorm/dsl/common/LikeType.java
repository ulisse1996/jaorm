package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public enum LikeType {

    FULL(" CONCAT('%',?,'%')"),
    START(" CONCAT('%',?)"),
    END(" CONCAT(?,'%')");

    private static final JaormLogger logger = JaormLogger.getLogger(LikeType.class);
    private final String value;

    LikeType(String value) {
        this.value = value;
    }

    public String getValue() {
        try {
            return VendorSpecific.getSpecific(LikeSpecific.class)
                    .convertToLikeSupport(LikeSpecific.LikeType.valueOf(name()));
        } catch (Exception ex) {
            logger.info("Can't find specific for like type , please contact author"::toString);
        }

        return value;
    }

    public String format(String joinTable, String joinedColumn) {
        return this.value.replace("?", String.format("%s.%s",joinTable, joinedColumn));
    }
}
