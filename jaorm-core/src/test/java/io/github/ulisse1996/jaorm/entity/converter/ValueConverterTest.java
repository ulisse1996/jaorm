package io.github.ulisse1996.jaorm.entity.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueConverterTest {

    @Test
    @SuppressWarnings("unchecked")
    void should_return_same_value_for_none_conversion() {
        Object val = new Object();
        ValueConverter<Object, Object> converter = (ValueConverter<Object, Object>) ValueConverter.NONE_CONVERTER;
        Assertions.assertEquals(val, converter.fromSql(val));
        Assertions.assertEquals(val, converter.toSql(val));
    }
}
