package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.DelegatesMock;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.function.Supplier;

class DelegatesServiceTest {

    private final DelegatesMock testSubject = new DelegatesMock();

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        try {
            Field field = DelegatesService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<DelegatesService> instance = (Singleton<DelegatesService>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

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
        Assertions.assertArrayEquals(new Object[]{entity.getField1(), entity.getField2()}, arguments.getValues());
    }

    @Test
    void should_return_sql() {
        String actual = testSubject.getSql(DelegatesMock.MyEntity.class);
        Assertions.assertEquals("SELECT FIELD1, FIELD2 FROM MYENTITY WHERE FIELD1 = ?", actual);
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

    @Test
    void should_find_delegate_from_entity() {
        Supplier<?> delegate = testSubject.searchDelegate(new DelegatesMock.MyEntity());
        Assertions.assertTrue(delegate.get() instanceof DelegatesMock.MyEntityDelegate);
    }

    @Test
    void should_get_where_arguments() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        entity.setField1("TEST_FIELD");
        Arguments arguments = testSubject.asWhere(entity);
        Assertions.assertArrayEquals(new Object[]{entity.getField1()}, arguments.getValues());
    }

    @Test
    void should_get_all_arguments() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        entity.setField1("TEST");
        entity.setField2(BigDecimal.ONE);
        Arguments arguments = testSubject.asArguments(entity);
        Assertions.assertEquals(2, arguments.getValues().length);
        Assertions.assertArrayEquals(new Object[]{entity.getField1(), entity.getField2()}, arguments.getValues());
    }

    @Test
    void should_get_update_sql() {
        String expected = "UPDATE MYENTITY SET FIELD FIELD1 = ?, FIELD2 = ? WHERE FIELD1 = ?";
        Assertions.assertEquals(expected, testSubject.getUpdateSql(DelegatesMock.MyEntity.class));
    }

    @Test
    void should_get_delete_sql() {
        String expected = "DELETE MYENTITY WHERE FIELD1 = ?";
        Assertions.assertEquals(expected, testSubject.getDeleteSql(DelegatesMock.MyEntity.class));
    }

    @Test
    void should_return_real_class_from_delegate_class() {
        Assertions.assertEquals(DelegatesMock.MyEntity.class, testSubject.getEntityClass(DelegatesMock.MyEntityDelegate.class));
    }

    @Test
    void should_create_insert_arguments_with_generated_values() {
        Arguments arguments = testSubject.asInsert(new DelegatesMock.MyEntity(), Collections.emptyMap());
        Assertions.assertEquals(2, arguments.getValues().length);
    }

    @Test
    void should_init_defaults() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        DelegatesMock.MyEntity result = testSubject.initDefaults(entity);
        Assertions.assertSame(entity, result);
    }

    @Test
    void should_return_false_for_default_generation() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        Assertions.assertFalse(testSubject.isDefaultGeneration(entity));
    }

    @Test
    void should_return_table_info() {
        TableInfo info = testSubject.getTableInfo(DelegatesMock.MyEntity.class);
        Assertions.assertEquals("TAB", info.getTable());
        Assertions.assertEquals(DelegatesMock.MyEntity.class, info.getEntity());
        Assertions.assertEquals(Table.UNSET, info.getSchema());
    }
}
