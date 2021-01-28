package io.jaorm.dsl;

import io.jaorm.dsl.common.EndSelect;
import io.jaorm.dsl.impl.SelectImpl;

import java.util.Objects;

public class Jaorm {

    private Jaorm() {
        throw new UnsupportedOperationException("No access for Jaorm !");
    }

    public static <T> EndSelect<T> select(Class<T> klass) {
        Objects.requireNonNull(klass, "Entity class can't be null !");
        return new SelectImpl().select(klass);
    }
}
