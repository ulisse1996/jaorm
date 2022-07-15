package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.common.Updated;
import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedExecutable;
import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdate;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdatedWhere;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdatedImpl<T> implements Updated<T>, UpdatedExecutable<T>, UpdatedWhere<T> {

    private final QueryConfig config;
    private final List<SetterImpl<T, ?>> setters;
    private final List<UpdatedWhereImpl<T, ?>> wheres;
    private final String table;
    private UpdatedWhereImpl<T, ?> lastWhere;

    public UpdatedImpl(Class<T> klass, QueryConfig queryConfig) {
        this.config = queryConfig;
        this.setters = new ArrayList<>();
        this.wheres = new ArrayList<>();
        this.table = DelegatesService.getInstance().searchDelegate(klass)
                .get()
                .getTable();
    }

    @Override
    public <R> IntermediateUpdate<T, R> setting(SqlColumn<T, R> column) {
        SetterImpl<T, R> update = new SetterImpl<>(this,
                Objects.requireNonNull(column, "Column can't be null !"));
        this.setters.add(update);
        return update;
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> where(SqlColumn<?, R> column) {
        return addAndReturnLast(column, false);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> where(VendorFunction<R> column) {
        return addAndReturnLast(column, false);
    }

    @Override
    public void execute() {
        String sql = asString();
        List<SqlParameter> params = Stream.concat(
                this.setters.stream()
                        .map(SetterImpl::getValue)
                        .filter(Objects::nonNull)
                        .map(SqlParameter::new),
                this.wheres.stream().flatMap(m -> m.getParameters(this.config.isCaseInsensitive()))
        ).collect(Collectors.toList());
        QueryRunner.getSimple().update(sql, params);
    }

    String asString() {
        StringBuilder builder = new StringBuilder("UPDATE ")
                .append(this.table)
                .append(" SET ")
                .append(formatSettings());
        return buildWheres(builder);
    }

    private String formatSettings() {
        AliasesSpecific specific = VendorSpecific.getSpecific(AliasesSpecific.class);
        return this.setters
                .stream()
                .map(s -> {
                    if (specific.isUpdateAliasRequired()) {
                        return String.format("%s.%s = %s",
                                this.table,
                                s.getColumn().getName(),
                                Optional.ofNullable(s.getFunction()).map(v -> v.apply(this.table)).orElse("?"));
                    } else {
                        return String.format("%s = %s",
                                s.getColumn().getName(),
                                Optional.ofNullable(s.getFunction()).map(v -> v.apply(this.table)).orElse("?"));
                    }
                })
                .collect(Collectors.joining(", "));
    }

    private String buildWheres(StringBuilder builder) {
        boolean first = true;
        for (UpdatedWhereImpl<?, ?> where : wheres) {
            if (first) {
                String gen = where.asString(true, this.config.isCaseInsensitive());
                builder.append(gen);
                if (!gen.trim().isEmpty()) {
                    first = false;
                }
            } else {
                builder.append(where.asString(false, this.config.isCaseInsensitive()));
            }
        }

        return builder.toString();
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> andWhere(SqlColumn<?, R> column) {
        return where(column);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> orWhere(SqlColumn<?, R> column) {
        return addAndReturnLast(column, true);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> and(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, false);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> or(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, true);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> andWhere(VendorFunction<R> function) {
        return where(function);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> orWhere(VendorFunction<R> function) {
        return addAndReturnLast(function, true);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> and(VendorFunction<R> function) {
        return addAndReturnLinked(function, false);
    }

    @Override
    public <R> IntermediateUpdatedWhere<T, R> or(VendorFunction<R> function) {
        return addAndReturnLinked(function, true);
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateUpdatedWhere<T,R> addAndReturnLast(SqlColumn<?,R> column, boolean or) {
        UpdatedWhereImpl<T, R> where = new UpdatedWhereImpl<>(column, this, or);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateUpdatedWhere<T, R>) this.lastWhere;
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateUpdatedWhere<T,R> addAndReturnLast(VendorFunction<R> function, boolean or) {
        UpdatedWhereImpl<T, R> where = new UpdatedWhereFunctionImpl<>(function, this, or);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateUpdatedWhere<T, R>) this.lastWhere;
    }

    private <R> IntermediateUpdatedWhere<T,R> addAndReturnLinked(SqlColumn<?, R> column, boolean or) {
        UpdatedWhereImpl<T, R> where = new UpdatedWhereImpl<>(column, this, or);
        this.lastWhere.addLinked(where);
        return where;
    }

    private <R> IntermediateUpdatedWhere<T,R> addAndReturnLinked(VendorFunction<R> column, boolean or) {
        UpdatedWhereImpl<T, R> where = new UpdatedWhereFunctionImpl<>(column, this, or);
        this.lastWhere.addLinked(where);
        return where;
    }

    public QueryConfig getConfig() {
        return config;
    }

    public String getTable() {
        return this.table;
    }
}
