package io.jaorm.dsl.impl;

public enum LikeType {
    FULL(" CONCAT('%', ?, '%')"),
    START(" CONCAT('%',?)"),
    END("CONCAT(?,'%')");

    private final String value;

    LikeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
