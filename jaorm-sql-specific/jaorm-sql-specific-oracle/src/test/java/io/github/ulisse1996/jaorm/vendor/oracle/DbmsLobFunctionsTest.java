package io.github.ulisse1996.jaorm.vendor.oracle;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.oracle.functions.DbmsLobFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DbmsLobFunctionsTest {

    private static final SqlColumn<Object, String> COL = SqlColumn.instance(Object.class, "COL", String.class);

    @Test
    void should_return_lobs_substr_function() {
        VendorFunction<String> substr = DbmsLobFunctions.substr(COL);
        Assertions.assertEquals("DBMS_LOB.SUBSTR(ALIAS.COL, 32000, 1)", substr.apply("ALIAS"));
    }

    @Test
    void should_return_lobs_substr_function_with_max_length() {
        VendorFunction<String> substr = DbmsLobFunctions.substr(COL, 10);
        Assertions.assertEquals("DBMS_LOB.SUBSTR(ALIAS.COL, 10, 1)", substr.apply("ALIAS"));
    }

    @Test
    void should_return_true_for_string_column() {
        VendorFunction<String> substr = DbmsLobFunctions.substr(COL, 10);
        Assertions.assertTrue(substr.isString());
    }
}
