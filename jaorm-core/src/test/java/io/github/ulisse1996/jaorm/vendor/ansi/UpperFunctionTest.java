package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UpperFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.instance(Object.class, "COL_1", String.class);

    @Test
    void should_create_upper_function() {
        VendorFunction<String> function = AnsiFunctions.upper(COL_1);
        Assertions.assertEquals(
                "UPPER(MY_TABLE.COL_1)",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }

    @Test
    void should_create_compound_upper_function() {
        VendorFunction<String> function = AnsiFunctions.upper(AnsiFunctions.lower(COL_1));
        Assertions.assertEquals(
                "UPPER(LOWER(MY_TABLE.COL_1))",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }
}
