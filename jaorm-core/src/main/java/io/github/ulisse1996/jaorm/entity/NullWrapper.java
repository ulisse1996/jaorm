package io.github.ulisse1996.jaorm.entity;

public class NullWrapper {

    private final Class<?> type;
    private final Object value;

    public NullWrapper(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
