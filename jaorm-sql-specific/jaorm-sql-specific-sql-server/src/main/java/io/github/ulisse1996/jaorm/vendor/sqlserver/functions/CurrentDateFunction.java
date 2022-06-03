package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.sql.Date;

public class CurrentDateFunction implements VendorFunction<Date> {

    public static final CurrentDateFunction INSTANCE = new CurrentDateFunction();

    private CurrentDateFunction() {}

    @Override
    public String apply(String alias) {
        return "CAST(CURRENT_TIMESTAMP as DATE)";
    }

    @Override
    public boolean isString() {
        return false;
    }
}
