package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.custom.CustomFeatures;

public enum LikeType {
    FULL(" CONCAT('%',?,'%')"),
    START(" CONCAT('%',?)"),
    END(" CONCAT(?,'%')");

    private final String value;

    LikeType(String value) {
        this.value = value;
    }

    public String getValue() {
        if (CustomFeatures.LIKE_FEATURE.isEnabled()) {
            return CustomFeatures.LIKE_FEATURE.getFeature().asSqlString(this.name());
        }
        return value;
    }

    public String format(String joinTable, String joinedColumn) {
        return this.value.replace("?", String.format("%s.%s",joinTable, joinedColumn));
    }
}
