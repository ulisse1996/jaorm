package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

public interface SqlColumn<T, R> {

    Class<T> getEntity();
    String getName();
    Class<R> getType();
    ValueConverter<?, R> getConverter(); //NOSONAR

    static <T,R> SqlColumn<T, R> instance(Class<T> entityClass, String name, Class<R> klass, ValueConverter<?, R> valueConverter) {
        return new SqlColumn<T, R>() {
            @Override
            public Class<T> getEntity() {
                return entityClass;
            }

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

    static <T,R> SqlColumn<T,R> instance(Class<T> entity, String name, Class<R> klass) {
        return instance(entity, name, klass, ValueConverter.none());
    }
}
