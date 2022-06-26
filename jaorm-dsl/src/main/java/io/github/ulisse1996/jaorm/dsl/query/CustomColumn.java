package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;

public class CustomColumn<R> implements SqlColumn<Object, R> {

    private final String name;
    private final Class<R> type;

    private CustomColumn(String name, Class<R> type) {
        this.name = name;
        SqlAccessor.find(type); // Check for valid type
        this.type = type;
    }

    public static <R> CustomColumn<R> of(String name, Class<R> type) {
        return new CustomColumn<>(name, type);
    }

    @Override
    public Class<Object> getEntity() {
        return Object.class;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<R> getType() {
        return this.type;
    }

    @Override
    public ValueConverter<?, R> getConverter() {
        return ValueConverter.none();
    }
}
