package io.github.ulisse1996.dsl;

import io.github.ulisse1996.dsl.impl.SelectImpl;
import io.github.ulisse1996.dsl.common.EndSelect;

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
