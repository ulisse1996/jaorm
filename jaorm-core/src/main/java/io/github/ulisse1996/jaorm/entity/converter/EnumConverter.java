package io.github.ulisse1996.jaorm.entity.converter;

import java.util.stream.Stream;

public abstract class EnumConverter<T extends Enum<T>> implements ValueConverter<String, T> {

    private final Class<T> klass;

    protected EnumConverter(Class<T> klass) {
        this.klass = klass;
    }

    @Override
    public T fromSql(String val) {
        return Stream.of(klass.getEnumConstants())
                .filter(el -> el.name().equalsIgnoreCase(toUpperCase(val)))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(this.klass, toUpperCase(val)));
    }

    private String toUpperCase(String val) {
        return val != null ? val.toUpperCase() : null;
    }

    @Override
    public String toSql(T val) {
        return val != null ? val.name() : null;
    }
}