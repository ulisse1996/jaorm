package io.jaorm.dsl.common;

public enum LikeType {
    FULL(" CONCAT('%',?,'%')"),
    START(" CONCAT('%',?)"),
    END(" CONCAT(?,'%')");

    private final String value;

    LikeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String format(String joinTable, String joinedColumn) {
        return this.value.replace("?", String.format("%s.%s",joinTable, joinedColumn));
    }
}
