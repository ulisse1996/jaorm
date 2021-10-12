package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.dsl.query.impl.SubQueryImpl;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.Objects;

public class QueryBuilder {

    private QueryBuilder() {
        throw new UnsupportedOperationException("No access for QueryBuilder");
    }

    public static <T> Selected<T> select(Class<T> klass) {
        return select(klass, QueryConfig.builder().build());
    }

    public static <T> Selected<T> select(Class<T> klass, boolean caseInsensitiveLike) {
        QueryConfig.Builder builder = QueryConfig.builder();
        if (caseInsensitiveLike) {
            builder = builder.caseInsensitive();
        }
        return select(klass, builder.build());
    }

    public static <T> Selected<T> select(Class<T> klass, QueryConfig config) {
        Objects.requireNonNull(klass, "Entity class can't be null !");
        return new SelectedImpl<>(klass, config);
    }

    public static <T, R> Selected<T> subQuery(SqlColumn<T, R> column) {
        return new SubQueryImpl<>(column);
    }
}
