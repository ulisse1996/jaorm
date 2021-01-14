package io.jaorm.entity.converter;

public interface ValueConverter<T,R> {

    R fromSql(T val);
    T toSql(R val);
}
