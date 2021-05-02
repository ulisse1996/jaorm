package io.github.ulisse1996.entity;

import io.github.ulisse1996.entity.converter.ValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class SqlColumnTest {

    @Test
    void should_create_new_instance() {
        SqlColumn<BigDecimal, BigDecimal> instance = SqlColumn.instance("COL1", BigDecimal.class);
        Assertions.assertEquals("COL1", instance.getName());
        Assertions.assertEquals(BigDecimal.class, instance.getType());
        Assertions.assertEquals(ValueConverter.NONE_CONVERTER, instance.getConverter());
    }
}
