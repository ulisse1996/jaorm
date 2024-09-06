package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.ConverterProvider;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.converter.EnumConverter;

@Table(name = "TABLE")
public class EntityWithEnum {

    @Id
    @Column(name = "COL1")
    @Converter(MyEnumConverter.class)
    private MyEnum col1;

    public MyEnum getCol1() {
        return col1;
    }

    public void setCol1(MyEnum col1) {
        this.col1 = col1;
    }

    public enum MyEnum {
        TEST
    }

    @ConverterProvider
    public static class MyEnumConverter extends EnumConverter<MyEnum> {

        public MyEnumConverter() {
            super(MyEnum.class);
        }
    }
}