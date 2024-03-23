package io.github.ulisse1996.jaorm.entity.converter;

import java.util.stream.Stream;

public abstract class EnumConverter<T extends Enum<T>> implements ValueConverter<String, T> {

    private final T[] values;
    private final Class<T> klass;

    protected EnumConverter(Class<T> klass) {
        this.klass = klass;
        this.values = klass.getEnumConstants();
    }

    @Override
    public T fromSql(String val) {
        return Stream.of(values)
                .filter(el -> el.name().equalsIgnoreCase(val))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(this.klass, val));
    }

    @Override
    public String toSql(T val) {
        return val != null ? val.name() : null;
    }
}