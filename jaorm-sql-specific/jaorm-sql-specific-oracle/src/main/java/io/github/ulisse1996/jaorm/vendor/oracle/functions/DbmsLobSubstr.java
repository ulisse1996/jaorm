package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.Objects;

class DbmsLobSubstr implements VendorFunction<String> {

    private final SqlColumn<?, String> column;
    private final int maxLength;

    DbmsLobSubstr(SqlColumn<?, String> column, int maxLength) {
        Objects.requireNonNull(column, "Column can't be null !");
        this.column = column;
        this.maxLength = maxLength;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.getColumnName(column, alias);
        return String.format("DBMS_LOB.SUBSTR(%s, %d, %d)", s, this.maxLength, 1);
    }

    @Override
    public boolean isString() {
        return true;
    }
}
