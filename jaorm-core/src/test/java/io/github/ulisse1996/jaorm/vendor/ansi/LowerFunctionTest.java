package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class LowerFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.instance(Object.class, "COL_1", String.class);

    @Test
    void should_create_lower_function() {
        VendorFunction<String> function = AnsiFunctions.lower(COL_1);
        Assertions.assertEquals(
                "LOWER(MY_TABLE.COL_1)",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }

    @Test
    void should_create_compound_lower_function() {
        VendorFunction<String> function = AnsiFunctions.lower(AnsiFunctions.upper(COL_1));
        Assertions.assertEquals(
                "LOWER(UPPER(MY_TABLE.COL_1))",
                function.apply("MY_TABLE")
        );
        Assertions.assertTrue(function.isString());
    }

    @Test
    void should_get_param_from_inline() {
        Assertions.assertEquals(
                Collections.singletonList("EL"),
                AnsiFunctions.lower(InlineValue.inline("EL")).getParams()
        );
    }
}