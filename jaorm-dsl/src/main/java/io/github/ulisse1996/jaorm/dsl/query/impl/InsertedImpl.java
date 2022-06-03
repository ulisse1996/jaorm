package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.Inserted;
import io.github.ulisse1996.jaorm.dsl.query.common.InsertedExecutable;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateInsert;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InsertedImpl<T> implements Inserted<T>, InsertedExecutable<T> {

    private final List<String> keyColumns;
    private final List<InsertingImpl<T, ?>> inserting;
    private final Class<T> klass;

    public InsertedImpl(Class<T> klass) {
        EntityMapper<?> entityMapper = DelegatesService.getInstance().searchDelegate(klass)
                .get()
                .getEntityMapper();
        this.klass = klass;
        this.keyColumns = entityMapper.getMappers().stream()
                .filter(EntityMapper.ColumnMapper::isKey)
                .map(EntityMapper.ColumnMapper::getName)
                .collect(Collectors.toList());
        this.inserting = new ArrayList<>();
    }

    @Override
    public <R> IntermediateInsert<T, R> column(SqlColumn<T, R> column) {
        InsertingImpl<T, R> insert = new InsertingImpl<>(this,
                Objects.requireNonNull(column, "Column can't be null !"));
        this.inserting.add(insert);
        return insert;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        boolean hasAllKeys = new HashSet<>(this.inserting
                .stream()
                .map(InsertingImpl::getColumn)
                .map(SqlColumn::getName)
                .collect(Collectors.toList()))
                .containsAll(this.keyColumns);
        if (!hasAllKeys) {
            throw new IllegalArgumentException(String.format("Missing insert keys %s", getMissingKeys()));
        }
        EntityDelegate<T> delegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(this.klass)
                .get();
        delegate.setFullEntityFullColumns(
                this.inserting.stream()
                        .map(i -> new Pair<>(i.getColumn(), i.getValue()))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
        );
        QueryRunner.getInstance(this.klass)
                .insert(delegate, delegate.getInsertSql(), DelegatesService.getInstance().asInsert(delegate).asSqlParameters());
    }

    private List<String> getMissingKeys() {
        List<String> missing = new ArrayList<>(this.keyColumns);
        missing.removeAll(
                this.inserting
                        .stream()
                        .map(InsertingImpl::getColumn)
                        .map(SqlColumn::getName)
                        .collect(Collectors.toList())
        );
        return missing;
    }
}
