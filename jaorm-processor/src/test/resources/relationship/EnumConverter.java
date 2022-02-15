package io.test;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

public class EnumConverter implements ValueConverter<String, CustomEnum> {

    public static final EnumConverter INSTANCE = new EnumConverter();

    @Override
    public CustomEnum fromSql(String val) {
        return CustomEnum.valueOf(val);
    }

    @Override
    public String toSql(CustomEnum val) {
        return val != null ? val.name() : null;
    }
}
