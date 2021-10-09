package io.github.ulisse1996.jaorm.dsl.query.enums;

public enum JoinType {
    JOIN(" JOIN "),
    LEFT_JOIN(" LEFT JOIN "),
    RIGHT_JOIN(" RIGHT JOIN "),
    FULL_JOIN(" FULL JOIN ");

    private final String value;

    JoinType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
