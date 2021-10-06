package io.github.ulisse1996.jaorm.dsl;

import io.github.ulisse1996.jaorm.dsl.impl.SelectImpl;
import io.github.ulisse1996.jaorm.dsl.common.EndSelect;

import java.util.Objects;

public class Jaorm {

    private Jaorm() {
        throw new UnsupportedOperationException("No access for Jaorm !");
    }

    public static <T> EndSelect<T> select(Class<T> klass) {
        return select(klass, false);
    }

    public static <T> EndSelect<T> select(Class<T> klass, boolean caseInsensitiveLike) {
        Objects.requireNonNull(klass, "Entity class can't be null !");
        return new SelectImpl().select(klass, caseInsensitiveLike);
    }
}
