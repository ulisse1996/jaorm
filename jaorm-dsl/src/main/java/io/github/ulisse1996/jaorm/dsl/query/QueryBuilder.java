package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.common.*;
import io.github.ulisse1996.jaorm.dsl.query.impl.*;
import io.github.ulisse1996.jaorm.dsl.query.impl.simple.AliasColumn;
import io.github.ulisse1996.jaorm.dsl.query.impl.simple.SimpleSelectedImpl;
import io.github.ulisse1996.jaorm.dsl.query.simple.FromSimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.SimpleSelected;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryBuilder {

    protected static final String ENTITY_CLASS_CAN_T_BE_NULL = "Entity class can't be null !";

    private QueryBuilder() {
        throw new UnsupportedOperationException("No access for QueryBuilder");
    }

    public static <T> Selected<T> select(Class<T> klass) {
        return select(klass, QueryConfig.builder().build());
    }

    public static <T> Selected<T> selectDistinct(Class<T> klass) {
        return selectDistinct(klass, QueryConfig.builder().build());
    }

    public static <T> Selected<T> select(Class<T> klass, boolean caseInsensitiveLike) {
        QueryConfig.Builder builder = QueryConfig.builder();
        if (caseInsensitiveLike) {
            builder = builder.caseInsensitive();
        }
        return select(klass, builder.build());
    }

    public static <T> Selected<T> selectDistinct(Class<T> klass, QueryConfig config) {
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new SelectedImpl<>(klass, config, true);
    }

    public static <T> Selected<T> select(Class<T> klass, QueryConfig config) {
        Objects.requireNonNull(klass, ENTITY_CLASS_CAN_T_BE_NULL);
        return new SelectedImpl<>(klass, config, false);
    }

    public static <T, R> Selected<T> subQuery(SqlColumn<T, R> column) {
        return subQuery(column, QueryConfig.builder().build());
    }

    public static <T, R> Selected<T> subQueryDistinct(SqlColumn<T, R> column) {
        return subQueryDistinct(column, QueryConfig.builder().build());
    }

    public static <T, R> Selected<T> subQueryDistinct(SqlColumn<T, R> column, QueryConfig config) {
        return new SubQueryImpl<>(column, config, true);
    }

    public static <T, R> Selected<T> subQuery(SqlColumn<T, R> column, QueryConfig config) {
        return new SubQueryImpl<>(column, config, false);
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

    public static SimpleSelected select(Selectable<?>... selectables) {
        return new SimpleSelectedImpl(
                Arrays.stream(selectables)
                        .map(el -> new AliasColumn(el, null))
                        .collect(Collectors.toList()),
                false
        );
    }

    public static SimpleSelected selectDistinct(Selectable<?>... selectables) {
        return new SimpleSelectedImpl(
                Arrays.stream(selectables)
                        .map(el -> new AliasColumn(el, null))
                        .collect(Collectors.toList()),
                true
        );
    }

    public static IntermediateSelected select(Selectable<?> selectable) {
        return new IntermediateSelected(selectable, null, false);
    }

    public static IntermediateSelected select(Selectable<?> selectable, String alias) {
        return new IntermediateSelected(selectable, alias, false);
    }

    public static IntermediateSelected selectDistinct(Selectable<?> selectable) {
        return new IntermediateSelected(selectable, null, true);
    }

    public static IntermediateSelected selectDistinct(Selectable<?> selectable, String alias) {
        return new IntermediateSelected(selectable, alias, true);
    }

    public static class IntermediateSelected implements SimpleSelected {

        private final List<AliasColumn> selectables;
        private QueryConfig config;
        private final boolean distinct;

        private IntermediateSelected(Selectable<?> selectable, String alias, boolean distinct) {
            this.distinct = distinct;
            this.selectables = new ArrayList<>();
            this.selectables.add(new AliasColumn(selectable, alias));
        }

        public IntermediateSelected select(Selectable<?> selectable, String alias) {
            this.selectables.add(new AliasColumn(selectable, alias));
            return this;
        }

        public IntermediateSelected select(Selectable<?> selectable) {
            return this.select(selectable, null);
        }

        @Override
        public IntermediateSelected withConfiguration(QueryConfig config) {
            this.config = config;
            return this;
        }

        @Override
        public FromSimpleSelected from(String table) {
            return from(table, null);
        }

        @Override
        public FromSimpleSelected from(String table, String alias) {
            SimpleSelected selected = new SimpleSelectedImpl(this.selectables, this.distinct);
            if (this.config != null) {
                selected = selected.withConfiguration(config);
            }
            return selected.from(table, alias);
        }
    }
}
