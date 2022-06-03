package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class AnsiFunctionsTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.instance(Object.class, "COL_1", String.class);

    @Test
    void should_create_upper_column() {
        VendorFunction<String> function = AnsiFunctions.upper(COL_1);
        Assertions.assertEquals(
                "UPPER(MY_TABLE.COL_1)",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }

    @Test
    void should_create_lower_column() {
        VendorFunction<String> function = AnsiFunctions.lower(COL_1);
        Assertions.assertEquals(
                "LOWER(MY_TABLE.COL_1)",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }

    @Test
    void should_throw_unsupported_exception_for_new_instance() {
        Constructor<?> declaredConstructor = AnsiFunctions.class.getDeclaredConstructors()[0];
        declaredConstructor.setAccessible(true);
        try {
            declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
            return;
        }

        Assertions.fail("Should throw unsupported exception !");
    }
}
