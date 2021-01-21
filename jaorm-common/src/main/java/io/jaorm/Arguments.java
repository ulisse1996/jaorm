package io.jaorm;

import java.util.Objects;

public interface Arguments {

    Object[] getValues();

    static Arguments of(Object... values) {
        return values(values);
    }

    static Arguments values(Object[] values) {
        Objects.requireNonNull(values);
        return () -> values;
    }

    static Arguments empty() {
        return () -> new Object[0];
    }
}
