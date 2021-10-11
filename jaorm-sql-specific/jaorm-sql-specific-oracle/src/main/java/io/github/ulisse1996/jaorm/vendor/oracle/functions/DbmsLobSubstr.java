package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.util.Objects;

public class DbmsLobSubstr implements VendorFunction<String> {

    private final SqlColumn<?, String> column;
    private final int maxLength;

    public DbmsLobSubstr(SqlColumn<?, String> column, int maxLength) {
        Objects.requireNonNull(column, "Column can't be null !");
        this.column = column;
        this.maxLength = maxLength;
    }

    @Override
    public String apply(String alias) {
        return String.format("DBMS_LOB.SUBSTR(%s.%s, %d, %d)", alias, column.getName(), this.maxLength, 1);
    }

    @Override
    public boolean isString() {
        return true;
    }
}
