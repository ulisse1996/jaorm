package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.common.*;
import io.github.ulisse1996.jaorm.dsl.query.impl.*;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.Objects;

public class QueryBuilder {

    protected static final String ENTITY_CLASS_CAN_T_BE_NULL = "Entity class can't be null !";

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
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new SelectedImpl<>(klass, config);
    }

    public static <T, R> Selected<T> subQuery(SqlColumn<T, R> column) {
        return new SubQueryImpl<>(column);
    }

    public static <T> Inserted<T> insertInto(Class<T> klass) {
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new InsertedImpl<>(klass);
    }

    public static <T> Updated<T> update(Class<T> klass) {
        return update(klass, QueryConfig.builder().build());
    }

    public static <T> Updated<T> update(Class<T> klass, QueryConfig queryConfig) {
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new UpdatedImpl<>(klass, queryConfig);
    }

    public static <T> Merge<T> merge(Class<T> klass) {
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new MergeImpl<>(klass);
    }

    public static <R> Case<R> usingCase() {
        return new CaseImpl<>();
    }
}
