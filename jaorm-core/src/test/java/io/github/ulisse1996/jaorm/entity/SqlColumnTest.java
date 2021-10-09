package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class SqlColumnTest {

    @Test
    void should_create_new_instance() {
        SqlColumn<Object, BigDecimal> instance = SqlColumn.instance(Object.class, "COL1", BigDecimal.class);
        Assertions.assertEquals(Object.class, instance.getEntity());
        Assertions.assertEquals("COL1", instance.getName());
        Assertions.assertEquals(BigDecimal.class, instance.getType());
        Assertions.assertEquals(ValueConverter.NONE_CONVERTER, instance.getConverter());
    }
}
