package io.github.ulisse1996.jaorm.entity;

public class NullWrapper {

    private final Class<?> type;

    public NullWrapper(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
