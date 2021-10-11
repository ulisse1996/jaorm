package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class DbmsLobFunctions {

    private static final int MAX_STRING_VALUE = 32000;
    private DbmsLobFunctions() {}

    public static VendorFunction<String> substr(SqlColumn<?, String> column, int maxLength) {
        return new DbmsLobSubstr(column, maxLength);
    }

    public static VendorFunction<String> substr(SqlColumn<?, String> column) {
        return new DbmsLobSubstr(column, MAX_STRING_VALUE);
    }
}
