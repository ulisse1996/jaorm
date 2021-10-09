package io.github.ulisse1996.jaorm.dsl.query.enums;

public enum Operation {
    EQUALS(" = "),
    NOT_EQUALS(" <> "),
    LESS_THAN(" < "),
    GREATER_THAN(" > "),
    LESS_EQUALS(" <= "),
    GREATER_EQUALS(" >= "),
    IS_NULL(""),
    IS_NOT_NULL(""),
    IN(""),
    NOT_IN(""),
    LIKE(""),
    NOT_LIKE("");

    private final String value;

    Operation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
