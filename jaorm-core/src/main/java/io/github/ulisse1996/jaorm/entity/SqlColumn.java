package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

@SuppressWarnings("unused")
public interface SqlColumn<T, R> { // NOSONAR We need parameter for DSL type safety

    String getName();
    Class<R> getType();
    ValueConverter<?, R> getConverter(); //NOSONAR

    static <T,R> SqlColumn<T, R> instance(String name, Class<R> klass, ValueConverter<?, R> valueConverter) {
        return new SqlColumn<T, R>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Class<R> getType() {
                return klass;
            }

            @Override
            public ValueConverter<?, R> getConverter() {
                return valueConverter;
            }
        };
    }

    static <T,R> SqlColumn<T,R> instance(String name, Class<R> klass) {
        return instance(name, klass, ValueConverter.none());
    }
}
