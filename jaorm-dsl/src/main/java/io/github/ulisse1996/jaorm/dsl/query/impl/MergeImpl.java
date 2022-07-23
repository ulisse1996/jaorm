package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.Merge;
import io.github.ulisse1996.jaorm.dsl.query.common.MergeEndMatching;
import io.github.ulisse1996.jaorm.dsl.query.common.MergeEndNotMatching;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.MergeUsing;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.MergedOn;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;
import io.github.ulisse1996.jaorm.dsl.util.Checker;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeImpl<T> implements Merge<T>, MergeUsing<T>, MergedOn<T>, MergeEndNotMatching<T>, MergeEndMatching<T> {

    private static final String COLUMN = "column";
    private final Class<T> klass;
    private final Map<SqlColumn<T, ?>, Object> usingColumns;
    private final List<SqlColumn<T, ?>> onColumns;
    private T insertEntity;
    private T updateEntity;

    public MergeImpl(Class<T> klass) {
        this.klass = klass;
        this.usingColumns = new HashMap<>();
        this.onColumns = new ArrayList<>();
    }

    @Override
    public <R> MergeUsing<T> using(SqlColumn<T, R> column, R value) {
        Checker.assertNotNull(column, COLUMN);
        Checker.assertNotNull(value, "value");
        this.usingColumns.put(column, value);
        return this;
    }

    @Override
    public <R> MergeUsing<T> using(SqlColumn<T, R> column, VendorFunction<R> function) {
        Checker.assertNotNull(column, COLUMN);
        Checker.assertNotNull(function, "function");
        this.usingColumns.put(column, function);
        return this;
    }

    @Override
    public MergedOn<T> onEquals(SqlColumn<T, ?> column) {
        Checker.assertNotNull(column, COLUMN);
        this.onColumns.add(column);
        return this;
    }

    @Override
    public MergeEndNotMatching<T> notMatchInsert(T entity) {
        Checker.assertNotNull(entity, "entity");
        this.insertEntity = entity;
        return this;
    }

    @Override
    public MergeEndMatching<T> matchUpdate(T entity) {
        Checker.assertNotNull(entity, "entity");
        this.updateEntity = entity;
        return this;
    }

    @Override
    public void execute() {
        VendorSpecific.getSpecific(MergeSpecific.class).executeMerge(
                this.klass,
                usingColumns,
                onColumns,
                updateEntity,
                insertEntity
        );
    }
}
