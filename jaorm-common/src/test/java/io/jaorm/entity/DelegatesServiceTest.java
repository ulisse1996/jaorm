package io.jaorm.entity;

import io.jaorm.Arguments;
import io.jaorm.DelegatesMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Supplier;

class DelegatesServiceTest {

    private final DelegatesMock testSubject = new DelegatesMock();

    @Test
    void should_return_same_delegate() {
        DelegatesMock.MyEntityDelegate expected = new DelegatesMock.MyEntityDelegate();
        Supplier<EntityDelegate<?>> entityDelegateSupplier = testSubject.searchDelegate(expected);
        Assertions.assertEquals(expected, entityDelegateSupplier.get());
    }

    @Test
    void should_get_all_columns() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        entity.setField1("TEST");
        entity.setField2(BigDecimal.ONE);
        Arguments arguments = testSubject.asInsert(entity);
        Assertions.assertEquals(2, arguments.getValues().length);
        Assertions.assertTrue(Arrays.equals(new Object[] {entity.getField1(), entity.getField2()}, arguments.getValues()));
    }

    @Test
    void should_return_sql() {
        String actual = testSubject.getSql(DelegatesMock.MyEntity.class);
        Assertions.assertEquals("SELECT FIELD1, FIELD2 FROM MYENTITY WHERE 1 = 1", actual);
    }

    @Test
    void should_return_simple_sql() {
        String actual = testSubject.getSimpleSql(DelegatesMock.MyEntity.class);
        Assertions.assertEquals("SELECT FIELD1, FIELD2 FROM MYENTITY", actual);
    }

    @Test
    void should_return_insert_sql() {
        String actual = testSubject.getInsertSql(new DelegatesMock.MyEntity());
        Assertions.assertEquals("INSERT INTO MYENTITY (FIELD1, FIELD2) VALUES (?,?)", actual);
    }
}