package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

public class SqlColumnWithAlias<T, R> implements SqlColumn<T, R> {

    private final SqlColumn<T, R> column;
    private final String alias;

    SqlColumnWithAlias(SqlColumn<T, R> column, String alias) {
        this.column = column;
        this.alias = alias;
    }

    public static <T, R> SqlColumnWithAlias<T,R> instance(SqlColumn<T,R> column, String alias) {
        return new SqlColumnWithAlias<>(column, alias);
    }

    public SqlColumn<T, R> getColumn() {
        return column;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public Class<T> getEntity() {
        return column.getEntity();
    }

    @Override
    public String getName() {
        return column.getName();
    }

    @Override
    public Class<R> getType() {
        return column.getType();
    }

    @Override
    public ValueConverter<?, R> getConverter() {
        return column.getConverter();
    }
}
