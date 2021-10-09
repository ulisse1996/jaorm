package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;

import java.util.Objects;

public class QueryBuilder {

    private QueryBuilder() {
        throw new UnsupportedOperationException("No access for QueryBuilder");
    }

    public static <T> Selected<T> select(Class<T> klass) {
        return select(klass, false);
    }

    public static <T> Selected<T> select(Class<T> klass, boolean caseInsensitiveLike) {
        Objects.requireNonNull(klass, "Entity class can't be null !");
        return new SelectedImpl<>(klass, caseInsensitiveLike);
    }
}
