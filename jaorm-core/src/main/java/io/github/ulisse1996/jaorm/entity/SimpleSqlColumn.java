package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

public class SimpleSqlColumn<R> implements SqlColumn<Object, R> {

    private final Class<R> klass;
    private final String name;

    public SimpleSqlColumn(String name, Class<R> klass) {
        this.name = name;
        this.klass = klass;
    }

    @Override
    public Class<Object> getEntity() {
        throw new UnsupportedOperationException("Unsupported operation for SimpleColumn !");
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<R> getType() {
        return this.klass;
    }

    @Override
    public ValueConverter<?, R> getConverter() {
        return ValueConverter.none();
    }
}
