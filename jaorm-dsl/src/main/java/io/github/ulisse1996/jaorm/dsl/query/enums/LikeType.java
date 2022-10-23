package io.github.ulisse1996.jaorm.dsl.query.enums;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;

public enum LikeType {

    FULL(" CONCAT('%',?,'%')", " CONCAT('%',UPPER(?),'%')"),
    START(" CONCAT('%',?)", " CONCAT('%',UPPER(?))"),
    END(" CONCAT(?,'%')", " CONCAT(UPPER(?),'%')");

    private static final JaormLogger logger = JaormLogger.getLogger(LikeType.class);
    private final String value;
    private final String caseInsensitiveValue;

    LikeType(String value, String caseInsensitiveValue) {
        this.value = value;
        this.caseInsensitiveValue = caseInsensitiveValue;
    }

    public String getValue(boolean caseInsensitiveLike) {
        try {
            return VendorSpecific.getSpecific(LikeSpecific.class)
                    .convertToLikeSupport(LikeSpecific.LikeType.valueOf(name()), caseInsensitiveLike);
        } catch (Exception ex) {
            logger.info("Can't find specific for like type , please contact author"::toString);
        }

        return caseInsensitiveLike ? caseInsensitiveValue : value;
    }

    public String format(String joinTable, String joinedColumn, boolean caseInsensitiveLike) {
        String sel = getValue(caseInsensitiveLike);
        return sel.replace("?", String.format("%s.%s",joinTable, joinedColumn));
    }

    public String format(String joinColumn, boolean caseInsensitiveLike) {
        String sel = getValue(caseInsensitiveLike);
        return sel.replace("?", joinColumn);
    }
}
