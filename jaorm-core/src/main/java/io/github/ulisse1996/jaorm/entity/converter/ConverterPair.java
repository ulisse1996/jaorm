package io.github.ulisse1996.jaorm.entity.converter;

public class ConverterPair<T, R> {

    private final Class<T> onSql;
    private final ValueConverter<T, R> converter;

    public ConverterPair(Class<T> onSql, ValueConverter<T, R> converter) {
        this.onSql = onSql;
        this.converter = converter;
    }

    public Class<T> getOnSql() {
        return onSql;
    }

    public ValueConverter<T, R> getConverter() {
        return converter;
    }
}
