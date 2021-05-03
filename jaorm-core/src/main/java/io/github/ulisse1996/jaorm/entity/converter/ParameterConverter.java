package io.github.ulisse1996.jaorm.entity.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

public enum ParameterConverter {
    NONE(String.class, s -> s),
    LONG(Long.class, Long::valueOf),
    BOOLEAN(Boolean.class, Boolean::valueOf),
    BIG_DECIMAL(BigDecimal.class, BigDecimal::new),
    BIG_INTEGER(BigInteger.class, BigInteger::new),
    INTEGER(Integer.class, Integer::valueOf),
    FLOAT(Float.class, Float::valueOf),
    DOUBLE(Double.class, Double::valueOf);

    private final Function<String, ?> mapper;
    private final Class<?> klass;

    ParameterConverter(Class<?> klass, Function<String, ?> mapper) {
        this.klass = klass;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public <R> R toValue(String s) {
        return (R) this.mapper.apply(s);
    }

    public Class<?> getKlass() {
        return klass;
    }
}
