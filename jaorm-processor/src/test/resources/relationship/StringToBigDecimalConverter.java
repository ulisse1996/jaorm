package io.test;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

import java.math.BigDecimal;

public class StringToBigDecimalConverter implements ValueConverter<String, BigDecimal> {

    public static final StringToBigDecimalConverter INSTANCE = new StringToBigDecimalConverter();

    @Override
    public BigDecimal fromSql(String val) {
        if (val != null && !val.isEmpty()) {
            return new BigDecimal(val);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String toSql(BigDecimal val) {
        return val != null ? val.toPlainString() : null;
    }
}
