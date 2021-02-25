package io.jaorm.entity.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

public enum ParameterConverter {
    NONE(s -> s),
    LONG(Long::valueOf),
    BOOLEAN(Boolean::valueOf),
    BIG_DECIMAL(BigDecimal::new),
    BIG_INTEGER(BigInteger::new),
    INTEGER(Integer::valueOf),
    FLOAT(Float::valueOf),
    DOUBLE(Double::valueOf);

    private final Function<String, ?> mapper;

    ParameterConverter(Function<String, ?> mapper) {
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public <R> R toValue(String s) {
        return (R) this.mapper.apply(s);
    }
}
