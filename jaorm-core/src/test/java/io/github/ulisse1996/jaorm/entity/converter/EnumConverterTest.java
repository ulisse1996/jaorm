package io.github.ulisse1996.jaorm.entity.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnumConverterTest {

    @Test
    void should_convert_to_sql() {
        Assertions.assertEquals("TEST", new MyEnumConverter().toSql(MyEnum.TEST));
    }

    @Test
    void should_convert_to_sql_null() {
        Assertions.assertNull(new MyEnumConverter().toSql(null));
    }

    @Test
    void should_convert_to_enum() {
        Assertions.assertSame(MyEnum.TEST, new MyEnumConverter().fromSql("TEST"));
    }

    @Test
    void should_throw_exception_for_missing_enum() {
        MyEnumConverter converter = new MyEnumConverter();
        Assertions.assertThrows(
                EnumConstantNotPresentException.class,
                () -> converter.fromSql("NOT_MY_ENUM")
        );
    }

    private enum MyEnum {
        TEST
    }

    private static class MyEnumConverter extends EnumConverter<MyEnum> {

        public MyEnumConverter() {
            super(MyEnum.class);
        }
    }
}