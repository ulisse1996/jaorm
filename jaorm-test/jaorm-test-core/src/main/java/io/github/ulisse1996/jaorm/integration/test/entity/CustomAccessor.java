package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

import java.math.BigDecimal;

@Table(name = "CUSTOM_ACCESSOR")
public class CustomAccessor {

    @Id
    @Column(name = "CUSTOM")
    @Converter(MyEnumCustom.Converter.class)
    private MyEnumCustom custom;

    public MyEnumCustom getCustom() {
        return custom;
    }

    public void setCustom(MyEnumCustom custom) {
        this.custom = custom;
    }

    public enum MyEnumCustom {
        VAL;

        public static class Converter implements ValueConverter<BigDecimal, MyEnumCustom> {

            public static final Converter INSTANCE = new Converter();

            @Override
            public MyEnumCustom fromSql(BigDecimal val) {
                return VAL;
            }

            @Override
            public BigDecimal toSql(MyEnumCustom val) {
                return BigDecimal.ONE;
            }
        }
    }
}
